package com.interrupt.utils;

public final class OSUtils
{
   private static String OS = null;
   
   public static String getOsName()
   {
      if(OS == null) {
          OS = System.getProperty("os.name");
          if(OS == null)
              return "unknown";
      }
      return OS.toLowerCase();
   }
   
   public static boolean isWindows()
   {
      return getOsName().startsWith("windows");
   }

   public static boolean isMac()
   {
       return getOsName().startsWith("mac");
   }
}