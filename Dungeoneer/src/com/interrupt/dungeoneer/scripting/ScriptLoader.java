package com.interrupt.dungeoneer.scripting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.game.ModManager;
import com.interrupt.utils.Logger;

import javax.tools.*;
import java.io.File;
import java.io.FilePermission;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.Permission;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PropertyPermission;

public class ScriptLoader implements ScriptingApi {

    private JavaCompiler compiler;
    private DiagnosticCollector<JavaFileObject> diagnostics;
    private StandardJavaFileManager fileManager;
    private List<String> optionList;

    final String modFolder = "java";

    @Override
    public void loadScripts(ModManager modManager) {
        ArrayMap<FileHandle, FileHandle[]> needsCompiling = modManager.getFilesForModsWithSuffix(modFolder, ".java");
        if (needsCompiling.size > 0) {
            // Setup the compiler
            compiler = ToolProvider.getSystemJavaCompiler();

            // try to compile
            if (compiler != null) {
                diagnostics = new DiagnosticCollector<JavaFileObject>();
                fileManager = compiler.getStandardFileManager(diagnostics, null, null);

                // Setup the compiler options
                optionList = new ArrayList<String>();
                optionList.add("-classpath");
                optionList.add(System.getProperty("java.class.path") + ";dist/InlineCompiler.jar");

                compileFiles(needsCompiling);
            }
        }

        Array<String> modClassNames = new Array<String>();

        // Go collect all the new class files
        ArrayMap<FileHandle, FileHandle[]> classFiles = modManager.getFilesForModsWithSuffix(modFolder, ".class");
        for (int i = 0; i < classFiles.size; i++) {
            FileHandle[] mod_classes = classFiles.getValueAt(i);

            for (int ii = 0; ii < mod_classes.length; ii++) {
                FileHandle classFile = mod_classes[ii];

                String classFilePath = classFile.pathWithoutExtension();
                String className = classFilePath.substring(classFilePath.indexOf(modFolder) + modFolder.length() + 1);

                // Add this class name so that we can sandbox it later
                modClassNames.add(className.replace('/', '.').replace('\\', '.'));
            }
        }

        // Sandbox any new packages if we found them
        if (modClassNames.size > 0) {
            // Make sandbox permissions, only gets read access to files and no network access
            Permissions permissions = new Permissions();
            permissions.add(new FilePermission("<<ALL FILES>>", "read"));
            permissions.add(new PropertyPermission("*", "read"));
            permissions.add(new ReflectPermission("suppressAccessChecks"));
            permissions.add(new RuntimePermission("accessDeclaredMembers"));

            ProtectionDomain protectionDomain = new ProtectionDomain(null, permissions);
            AccessControlContext accessContext = new AccessControlContext(new ProtectionDomain[]{protectionDomain});

            System.setSecurityManager(new SandboxSecurityManager(modClassNames, accessContext));
        }

        // Finally, let the game know about these new classes
        Array<FileHandle> classFolders = modManager.getFileInAllMods(modFolder);
        for (int i = 0; i < classFolders.size; i++) {
            FileHandle dir = classFolders.get(i);
            addDirectoryToClassLoader(dir);
        }

        // Let the GC eat all of this when we are done
        compiler = null;
        diagnostics = null;
        fileManager = null;
        optionList = null;
    }

    private void compileFiles(ArrayMap<FileHandle, FileHandle[]> filesByDirectory) {
        for (int i = 0; i < filesByDirectory.size; i++) {
            try {
                FileHandle dir = filesByDirectory.getKeyAt(i);
                FileHandle[] fileHandles = filesByDirectory.getValueAt(i);

                // Get base files
                File[] files = new File[fileHandles.length];
                for (int ii = 0; ii < fileHandles.length; ii++) {
                    files[ii] = fileHandles[ii].file();
                    Gdx.app.log("Scripting", "Found script: " + files[ii]);
                }

                Iterable<? extends JavaFileObject> compilationUnit
                        = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));

                JavaCompiler.CompilationTask task = compiler.getTask(
                        null,
                        fileManager,
                        diagnostics,
                        optionList,
                        null,
                        compilationUnit);

                if (!task.call()) {
                    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                        String fileName = diagnostic.getSource().getName();
                        Logger.logExceptionToFile("Scripting", "Failed to compile Java file: " + fileName, new Exception(diagnostic.toString()));
                    }
                }
            }
            catch (Exception ex) {
                Logger.logExceptionToFile("Scripting", "Failed to compile Java files.", ex);
            }
        }
    }

    private void addDirectoryToClassLoader(FileHandle directory) {
        try {
            // Use reflection to get the "addURL" method to add this directory to the classpath at runtime
            URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class sysclass = URLClassLoader.class;

            URL url = directory.file().toURI().toURL();

            Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{url});
        }
        catch (Exception ex) {
            Logger.logExceptionToFile("Scripting", "Failed to add directory to ClassLoader", ex);
        }
    }

    private class SandboxSecurityManager extends SecurityManager {

        private final ArrayMap<String, Boolean> checkedClassNames = new ArrayMap<String, Boolean>();
        private final AccessControlContext sandboxContext;

        SandboxSecurityManager(Array<String> classNamesToRestrict, AccessControlContext sandboxContext) {
            for (String className : classNamesToRestrict) {
                checkedClassNames.put(className, true);
            }
            this.sandboxContext = sandboxContext;
        }

        @Override
        public void checkPermission(Permission permission, Object context) {
            checkPermission(permission);
        }

        @Override
        public void checkPermission(Permission permission) {
            for (Class<?> clasS : this.getClassContext()) {
                if (checkedClassNames.containsKey(clasS.getName())) {
                    try {
                        sandboxContext.checkPermission(permission);
                    }
                    catch (SecurityException ex) {
                        Gdx.app.error("Scripting", ex.getMessage(), ex);
                        throw ex;
                    }
                }
            }
        }
    }
}
