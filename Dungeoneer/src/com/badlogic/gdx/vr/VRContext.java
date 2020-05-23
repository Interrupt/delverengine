package com.badlogic.gdx.vr;

import static org.lwjgl.openvr.VR.VR_ShutdownInternal;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.openvr.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Responsible for initializing the VR system, managing rendering surfaces,
 * getting tracking device poses, submitting the rendering results to the HMD
 * and rendering the surfaces side by side to the companion window on the
 * desktop. Wrapper around OpenVR.
 *
 * 	FIXME add multisampling plus draw/resolve buffers
 */
public class VRContext implements Disposable {
    /** device index of the head mounted display **/
    public static final int HMD_DEVICE_INDEX = VR.k_unTrackedDeviceIndex_Hmd;

    /** maximum device index **/
    public static final int MAX_DEVICE_INDEX = VR.k_unMaxTrackedDeviceCount - 1;

    // Chad: Hacky world scaling option, probably going to cause other issues
    public static float WORLD_SCALE = 2.15f;

    /**
     * Space in which matrices and vectors are returned in by
     * {@link VRDevice} methods taking a {@link Space}.
     * In case {@link Space#World} is specified, all values
     * are transformed by the {@link Matrix4} set via
     * {@link VRContext#setTrackerSpaceOriginToWorldSpaceTransform(Matrix4)}.
     */
    public static enum Space {
        Tracker,
        World
    }

    /**
     * Used to select for which eye a specific property should be accessed.
     */
    public static enum Eye {
        Left(0), Right(1);

        final int index;

        Eye(int index) {
            this.index = index;
        }
    }

    /**
     * Type of a {@link VRDevice}
     */
    public static enum VRDeviceType {
        /** the head mounted display **/
        HeadMountedDisplay,
        /** a controller like Oculus touch or HTC Vice controller **/
        Controller,
        /** a camera/base station tracking the HMD and/or controllers **/
        BaseStation,
        /** a generic VR tracking device **/
        Generic
    }

    /**
     * The role of a {@link VRDevice} of type {@link VRDeviceType#Controller}
     */
    public static enum VRControllerRole {
        Unknown,
        LeftHand,
        RightHand
    }

    /**
     * Button ids on VR controllers
     */
    public static class VRControllerButtons {
        public static final int System = 0;
        public static final int ApplicationMenu	= 1;
        public static final int Grip = 2;
        public static final int DPad_Left = 3;
        public static final int DPad_Up = 4;
        public static final int DPad_Right = 5;
        public static final int DPad_Down = 6;
        public static final int A = 7;

        public static final int ProximitySensor = 31;

        public static final int Axis0 = 32;
        public static final int Axis1 = 33;
        public static final int Axis2 = 34;
        public static final int Axis3 = 35;
        public static final int Axis4 = 36;

        // aliases for well known controllers
        public static final int SteamVR_Touchpad = Axis0;
        public static final int SteamVR_Trigger	= Axis1;

        public static final int Dashboard_Back = Grip;
    }

    /**
     * Axes ids on VR controllers
     */
    public static class VRControllerAxes {
        public static final int Axis0 = 0;
        public static final int Axis1 = 1;
        public static final int Axis2 = 2;
        public static final int Axis3 = 3;
        public static final int Axis4 = 4;

        // aliases for known controllers
        public static final int SteamVR_Touchpad = Axis0;
        public static final int SteamVR_Trigger = Axis1;
    }

    public static enum VRDeviceProperty {
        Invalid								(0),

        // general properties that apply to all device classes
        TrackingSystemName_String				(1000),
        ModelNumber_String						(1001),
        SerialNumber_String					(1002),
        RenderModelName_String					(1003),
        WillDriftInYaw_Bool					(1004),
        ManufacturerName_String				(1005),
        TrackingFirmwareVersion_String			(1006),
        HardwareRevision_String				(1007),
        AllWirelessDongleDescriptions_String	(1008),
        ConnectedWirelessDongle_String			(1009),
        DeviceIsWireless_Bool					(1010),
        DeviceIsCharging_Bool					(1011),
        DeviceBatteryPercentage_Float			(1012), // 0 is empty), 1 is full
        // FIXME
        // StatusDisplayTransform_Matrix34		(1013),
        Firmware_UpdateAvailable_Bool			(1014),
        Firmware_ManualUpdate_Bool				(1015),
        Firmware_ManualUpdateURL_String		(1016),
        HardwareRevision_Uint64				(1017),
        FirmwareVersion_Uint64					(1018),
        FPGAVersion_Uint64						(1019),
        VRCVersion_Uint64						(1020),
        RadioVersion_Uint64					(1021),
        DongleVersion_Uint64					(1022),
        BlockServerShutdown_Bool				(1023),
        CanUnifyCoordinateSystemWithHmd_Bool	(1024),
        ContainsProximitySensor_Bool			(1025),
        DeviceProvidesBatteryStatus_Bool		(1026),
        DeviceCanPowerOff_Bool					(1027),
        Firmware_ProgrammingTarget_String		(1028),
        DeviceClass_Int32						(1029),
        HasCamera_Bool							(1030),
        DriverVersion_String                   (1031),
        Firmware_ForceUpdateRequired_Bool      (1032),
        ViveSystemButtonFixRequired_Bool		(1033),

        // Properties that are unique to TrackedDeviceClass_HMD
        ReportsTimeSinceVSync_Bool				(2000),
        SecondsFromVsyncToPhotons_Float		(2001),
        DisplayFrequency_Float					(2002),
        UserIpdMeters_Float					(2003),
        CurrentUniverseId_Uint64				(2004),
        PreviousUniverseId_Uint64				(2005),
        DisplayFirmwareVersion_Uint64			(2006),
        IsOnDesktop_Bool						(2007),
        DisplayMCType_Int32					(2008),
        DisplayMCOffset_Float					(2009),
        DisplayMCScale_Float					(2010),
        EdidVendorID_Int32						(2011),
        DisplayMCImageLeft_String              (2012),
        DisplayMCImageRight_String             (2013),
        DisplayGCBlackClamp_Float				(2014),
        EdidProductID_Int32					(2015),
        // FIXME
        // CameraToHeadTransform_Matrix34			(2016),
        DisplayGCType_Int32					(2017),
        DisplayGCOffset_Float					(2018),
        DisplayGCScale_Float					(2019),
        DisplayGCPrescale_Float				(2020),
        DisplayGCImage_String					(2021),
        LensCenterLeftU_Float					(2022),
        LensCenterLeftV_Float					(2023),
        LensCenterRightU_Float					(2024),
        LensCenterRightV_Float					(2025),
        UserHeadToEyeDepthMeters_Float			(2026),
        CameraFirmwareVersion_Uint64			(2027),
        CameraFirmwareDescription_String		(2028),
        DisplayFPGAVersion_Uint64				(2029),
        DisplayBootloaderVersion_Uint64		(2030),
        DisplayHardwareVersion_Uint64			(2031),
        AudioFirmwareVersion_Uint64			(2032),
        CameraCompatibilityMode_Int32			(2033),
        ScreenshotHorizontalFieldOfViewDegrees_Float (2034),
        ScreenshotVerticalFieldOfViewDegrees_Float (2035),
        DisplaySuppressed_Bool					(2036),
        DisplayAllowNightMode_Bool				(2037),

        // Properties that are unique to TrackedDeviceClass_Controller
        AttachedDeviceId_String				(3000),
        SupportedButtons_Uint64				(3001),
        Axis0Type_Int32						(3002), // Return value is of type EVRControllerAxisType
        Axis1Type_Int32						(3003), // Return value is of type EVRControllerAxisType
        Axis2Type_Int32						(3004), // Return value is of type EVRControllerAxisType
        Axis3Type_Int32						(3005), // Return value is of type EVRControllerAxisType
        Axis4Type_Int32						(3006), // Return value is of type EVRControllerAxisType
        ControllerRoleHint_Int32				(3007), // Return value is of type ETrackedControllerRole

        // Properties that are unique to TrackedDeviceClass_TrackingReference
        FieldOfViewLeftDegrees_Float			(4000),
        FieldOfViewRightDegrees_Float			(4001),
        FieldOfViewTopDegrees_Float			(4002),
        FieldOfViewBottomDegrees_Float			(4003),
        TrackingRangeMinimumMeters_Float		(4004),
        TrackingRangeMaximumMeters_Float		(4005),
        ModeLabel_String						(4006),

        // Properties that are used for user interface like icons names
        IconPathName_String						(5000), // usually a directory named "icons"
        NamedIconPathDeviceOff_String				(5001), // PNG for static icon), or GIF for animation), 50x32 for headsets and 32x32 for others
        NamedIconPathDeviceSearching_String		(5002), // PNG for static icon), or GIF for animation), 50x32 for headsets and 32x32 for others
        NamedIconPathDeviceSearchingAlert_String	(5003), // PNG for static icon), or GIF for animation), 50x32 for headsets and 32x32 for others
        NamedIconPathDeviceReady_String			(5004), // PNG for static icon), or GIF for animation), 50x32 for headsets and 32x32 for others
        NamedIconPathDeviceReadyAlert_String		(5005), // PNG for static icon), or GIF for animation), 50x32 for headsets and 32x32 for others
        NamedIconPathDeviceNotReady_String			(5006), // PNG for static icon), or GIF for animation), 50x32 for headsets and 32x32 for others
        NamedIconPathDeviceStandby_String			(5007), // PNG for static icon), or GIF for animation), 50x32 for headsets and 32x32 for others
        NamedIconPathDeviceAlertLow_String			(5008), // PNG for static icon), or GIF for animation), 50x32 for headsets and 32x32 for others

        // Vendors are free to expose private debug data in this reserved region
        VendorSpecific_Reserved_Start			(10000),
        VendorSpecific_Reserved_End			(10999);

        public final int value;

        VRDeviceProperty(int value) {
            this.value = value;
        }
    }

    // couple of scratch buffers
    private final IntBuffer error = BufferUtils.newIntBuffer(1);
    private final IntBuffer scratch = BufferUtils.newIntBuffer(1), scratch2 = BufferUtils.newIntBuffer(1);

    // per eye data such as rendering surfaces, textures, regions, cameras etc. for each eye
    private final VRPerEyeData perEyeData[] = new VRPerEyeData[2];

    // batcher to draw eye rendering surface to companion window
    private final SpriteBatch batcher;

    // internal native objects to get device poses
    private final TrackedDevicePose.Buffer trackedDevicePoses = TrackedDevicePose.create(VR.k_unMaxTrackedDeviceCount);
    private final TrackedDevicePose.Buffer trackedDeviceGamePoses = TrackedDevicePose.create(VR.k_unMaxTrackedDeviceCount);

    // devices, their poses and listeners
    private final VRDevicePose[] devicePoses = new VRDevicePose[VR.k_unMaxTrackedDeviceCount];
    private final VRDevice[] devices = new VRDevice[VR.k_unMaxTrackedDeviceCount];
    private final Array<VRDeviceListener> listeners = new Array<VRDeviceListener>();
    private final VREvent event = VREvent.create();

    // render models
    private final ObjectMap<String, Model> models = new ObjectMap<String, Model>();

    // book keeping
    private Eye currentEye = null;
    private boolean renderingStarted = false;
    private boolean initialDevicesReported = false;

    // offsets for translation and rotation from tracker to world space
    private final Vector3 trackerSpaceOriginToWorldSpaceTranslationOffset = new Vector3();
    private final Matrix4 trackerSpaceToWorldspaceRotationOffset = new Matrix4();

    /**
     * Creates a new VRContext, initializes the VR system, and
     * sets up rendering surfaces with depth attachments.
     */
    public VRContext() {
        this(1, false);
    }

    /**
     * Creates a new VRContext, initializes the VR system,
     * and sets up rendering surfaces.
     *
     * @param renderTargetMultiplier multiplier to scale the render surface dimensions as a replacement for multisampling
     * @param hasStencil whether the rendering surfaces should have a stencil buffer
     * @throws {@link GdxRuntimeException} if the system could not be initialized
     */
    public VRContext(float renderTargetMultiplier, boolean hasStencil) {
        int token = VR.VR_InitInternal(error, VR.EVRApplicationType_VRApplication_Scene);
        checkInitError(error);
        OpenVR.create(token);

        VR.VR_GetGenericInterface(VR.IVRCompositor_Version, error);
        checkInitError(error);

        VR.VR_GetGenericInterface(VR.IVRRenderModels_Version, error);
        checkInitError(error);

        for (int i = 0; i < devicePoses.length; i++) {
            devicePoses[i] = new VRDevicePose(i);
        }

        VRSystem.VRSystem_GetRecommendedRenderTargetSize(scratch, scratch2);
        int width = (int)(scratch.get(0) * renderTargetMultiplier);
        int height = (int)(scratch2.get(0) * renderTargetMultiplier);

        setupEye(Eye.Left, width, height, hasStencil);
        setupEye(Eye.Right, width, height, hasStencil);

        batcher = new SpriteBatch();
    }

    private void setupEye(Eye eye, int width, int height, boolean hasStencil) {
        FrameBuffer buffer = new FrameBuffer(Format.RGBA8888, width, height, true, hasStencil);
        TextureRegion region = new TextureRegion(buffer.getColorBufferTexture());
        region.flip(false, true);
        VRCamera camera = new VRCamera(this, eye);
        camera.near = 0.1f;
        camera.far = 1000f;
        perEyeData[eye.index] = new VRPerEyeData(buffer, region, camera);
    }

    private void checkInitError(IntBuffer errorBuffer) {
        if (errorBuffer.get(0) != VR.EVRInitError_VRInitError_None) {
            int error = errorBuffer.get(0);
            throw new GdxRuntimeException("VR Initialization error: " + VR.VR_GetVRInitErrorAsEnglishDescription(error));
        }
    }

    /**
     * Returns the tracker space to world space translation offset. All positional vectors
     * returned by {@link VRDevice} methods taking a {@link Space#World} are
     * multiplied offset by this vector. This allows offsetting {@link VRDevice}
     * positions and orientations in world space.
     */
    public Vector3 getTrackerSpaceOriginToWorldSpaceTranslationOffset () {
        return trackerSpaceOriginToWorldSpaceTranslationOffset;
    }

    /**
     * Returns the tracker space to world space rotation offset. All rotational vectors
     * returned by {@link VRDevice} methods taking a {@link Space#World} are
     * rotated by this offset. This allows offsetting {@link VRDevice}
     * orientations in world space. The matrix needs to only have
     * rotational components.
     */
    public Matrix4 getTrackerSpaceToWorldspaceRotationOffset () {
        return trackerSpaceToWorldspaceRotationOffset;
    }

    /**
     * Adds a {@link VRDeviceListener} to receive events
     */
    public void addListener(VRDeviceListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a {@link VRDeviceListener}
     */
    public void removeListener(VRDeviceListener listener) {
        this.listeners.removeValue(listener, true);
    }

    /**
     * @return the first {@link VRDevice} of the given {@link VRDeviceType} or null.
     */
    public VRDevice getDeviceByType(VRDeviceType type) {
        for (VRDevice d: devices) {
            if (d != null && d.getType() == type) return d;
        }
        return null;
    }

    /**
     * @return all {@link VRDevice} instances of the given {@link VRDeviceType}.
     */
    public Array<VRDevice> getDevicesByType(VRDeviceType type) {
        Array<VRDevice> result = new Array<VRDevice>();
        for (VRDevice d: devices) {
            if (d != null && d.getType() == type) result.add(d);
        }
        return result;
    }

    /**
     * @return all currently connected {@link VRDevice} instances.
     */
    public Array<VRDevice> getDevices() {
        Array<VRDevice> result = new Array<VRDevice>();
        for (VRDevice d: devices) {
            if (d != null) result.add(d);
        }
        return result;
    }

    /**
     * @return the {@link VRDevice} of ype {@link VRDeviceType#Controller} that matches the role, or null.
     */
    public VRDevice getControllerByRole(VRControllerRole role) {
        for (VRDevice d: devices) {
            if (d != null && d.getType() == VRDeviceType.Controller && d.getControllerRole() == role) return d;
        }
        return null;
    }

    VRDevicePose getDevicePose(int deviceIndex) {
        if (deviceIndex < 0 || deviceIndex >= devicePoses.length) throw new IndexOutOfBoundsException("Device index must be >= 0 and <= " + devicePoses.length);
        return devicePoses[deviceIndex];
    }

    /**
     * Start rendering. Call beginEye to setup rendering
     * for each individual eye. End rendering by calling
     * #end
     */
    public void begin() {
        if (renderingStarted) throw new GdxRuntimeException("Last begin() call not completed, call end() before starting a new render");
        renderingStarted = true;

        perEyeData[Eye.Left.index].camera.update();
        perEyeData[Eye.Right.index].camera.update();
    }

    /**
     * Get the latest tracking data and send events to {@link VRDeviceListener} instance registered with the context.
     *
     * Must be called before begin!
     */
    public void pollEvents() {
        VRCompositor.VRCompositor_WaitGetPoses(trackedDevicePoses, trackedDeviceGamePoses);

        if (!initialDevicesReported) {
            for (int index = 0; index < devices.length; index++) {
                if (VRSystem.VRSystem_IsTrackedDeviceConnected(index)) {
                    createDevice(index);
                    for (VRDeviceListener l: listeners) {
                        l.connected(devices[index]);
                    }
                }
            }
            initialDevicesReported = true;
        }

        for (int device = 0; device < VR.k_unMaxTrackedDeviceCount; device++) {
            TrackedDevicePose trackedPose = trackedDevicePoses.get(device);
            VRDevicePose pose = devicePoses[device];

            hmdMat34ToMatrix4(trackedPose.mDeviceToAbsoluteTracking(), pose.transform);
            pose.velocity.set(trackedPose.vVelocity().v(0), trackedPose.vVelocity().v(1), trackedPose.vVelocity().v(2));
            pose.angularVelocity.set(trackedPose.vAngularVelocity().v(0), trackedPose.vAngularVelocity().v(1), trackedPose.vAngularVelocity().v(2));
            pose.isConnected = trackedPose.bDeviceIsConnected();
            pose.isValid = trackedPose.bPoseIsValid();

            if (devices[device] != null) {
                devices[device].updateAxesAndPosition();
                if (devices[device].modelInstance != null) {
                    devices[device].modelInstance.transform.idt().translate(trackerSpaceOriginToWorldSpaceTranslationOffset).mul(trackerSpaceToWorldspaceRotationOffset).mul(pose.transform);
                }
            }
        }

        while (VRSystem.VRSystem_PollNextEvent(event)) {
            int index = event.trackedDeviceIndex();
            if (index < 0 || index > VR.k_unMaxTrackedDeviceCount) continue;
            int button = 0;

            switch (event.eventType()) {
                case VR.EVREventType_VREvent_TrackedDeviceActivated:
                    createDevice(index);
                    for (VRDeviceListener l: listeners) {
                        l.connected(devices[index]);
                    }
                    break;
                case VR.EVREventType_VREvent_TrackedDeviceDeactivated:
                    index = event.trackedDeviceIndex();
                    if (devices[index] == null) continue;
                    for (VRDeviceListener l: listeners) {
                        l.disconnected(devices[index]);
                    }
                    devices[index] = null;
                    break;
                case VR.EVREventType_VREvent_ButtonPress:
                    if (devices[index] == null) continue;
                    button = event.data().controller().button();
                    devices[index].setButton(button, true);
                    for (VRDeviceListener l: listeners) {
                        l.buttonPressed(devices[index], button);
                    }
                    break;
                case VR.EVREventType_VREvent_ButtonUnpress:
                    if (devices[index] == null) continue;
                    button = event.data().controller().button();
                    devices[index].setButton(button, false);
                    for (VRDeviceListener l: listeners) {
                        l.buttonReleased(devices[index], button);
                    }
                    break;
                default:
                    Gdx.app.log("VRContext", "Unknown event: " + event.eventType());
            }
        }
    }

    private void createDevice(int index) {
        VRDeviceType type = null;
        int deviceClass = VRSystem.VRSystem_GetTrackedDeviceClass(index);
        switch(deviceClass) {
            case VR.ETrackedDeviceClass_TrackedDeviceClass_HMD:
                type = VRDeviceType.HeadMountedDisplay;
                break;
            case VR.ETrackedDeviceClass_TrackedDeviceClass_Controller:
                type = VRDeviceType.Controller;
                break;
            case VR.ETrackedDeviceClass_TrackedDeviceClass_TrackingReference:
                type = VRDeviceType.BaseStation;
                break;
            case VR.ETrackedDeviceClass_TrackedDeviceClass_GenericTracker:
                type = VRDeviceType.Generic;
                break;
            default:
                return;
        }

        VRControllerRole role = VRControllerRole.Unknown;
        if (type == VRDeviceType.Controller) {
            int r =  VRSystem.VRSystem_GetControllerRoleForTrackedDeviceIndex(index);
            switch(r) {
                case VR.ETrackedControllerRole_TrackedControllerRole_LeftHand:
                    role = VRControllerRole.LeftHand;
                    break;
                case VR.ETrackedControllerRole_TrackedControllerRole_RightHand:
                    role = VRControllerRole.RightHand;
                    break;
            }
        }
        devices[index] = new VRDevice(devicePoses[index], type, role);
        devices[index].updateAxesAndPosition();
    }

    /**
     * @return the {@link VRPerEyeData} such as rendering surface and camera
     */
    public VRPerEyeData getEyeData(Eye eye) {
        return perEyeData[eye.index];
    }

    /**
     * Start rendering to the rendering surface for the given eye.
     * Complete by calling {@link #endEye()}.
     */
    public void beginEye(Eye eye) {
        if (!renderingStarted) throw new GdxRuntimeException("Call begin() before calling beginEye()");
        if (currentEye != null) throw new GdxRuntimeException("Last beginEye() call not completed, call endEye() before starting a new render");
        currentEye = eye;
        perEyeData[eye.index].buffer.begin();
    }

    /**
     * Completes rendering to the rendering surface for the given eye.
     */
    public void endEye() {
        if (currentEye == null) throw new GdxRuntimeException("Call beginEye() before endEye()");
        perEyeData[currentEye.index].buffer.end();
        currentEye = null;
    }

    /**
     * Completes rendering and submits the rendering surfaces to the
     * head mounted display.
     */
    public void end() {
        if (!renderingStarted) throw new GdxRuntimeException("Call begin() before end()");
        renderingStarted = false;

        VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Left, perEyeData[Eye.Left.index].texture, null, VR.EVRSubmitFlags_Submit_Default);
        VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Right, perEyeData[Eye.Right.index].texture, null, VR.EVRSubmitFlags_Submit_Default);
    }

    public void dispose() {
        for (VRPerEyeData eyeData: perEyeData)
            eyeData.buffer.dispose();
        batcher.dispose();
        VR_ShutdownInternal();
    }

    /**
     * Resizes the companion window so the rendering buffers
     * can be displayed without stretching.
     */
    public void resizeCompanionWindow() {
        GLFrameBuffer<Texture> buffer = perEyeData[0].buffer;
        Gdx.graphics.setWindowedMode(buffer.getWidth(), buffer.getHeight());
    }

    /**
     * Renders the content of the given eye's rendering surface
     * to the entirety of the companion window.
     */
    public void renderToCompanionWindow(Eye eye) {
        GLFrameBuffer<Texture> buffer = perEyeData[eye.index].buffer;
        TextureRegion region = perEyeData[eye.index].region;
        batcher.getProjectionMatrix().setToOrtho2D(0, 0, buffer.getWidth(), buffer.getHeight());
        batcher.begin();
        batcher.draw(region, 0, 0);
        batcher.end();
    }

    private Model loadRenderModel(String name) {
        if (models.containsKey(name)) return models.get(name);

        // FIXME we load the models synchronously cause we are lazy
        int error = 0;
        PointerBuffer modelPointer = PointerBuffer.allocateDirect(1);
        while (true) {
            error = VRRenderModels.VRRenderModels_LoadRenderModel_Async(name, modelPointer);
            if (error != VR.EVRRenderModelError_VRRenderModelError_Loading) break;
        }

        if (error != VR.EVRRenderModelError_VRRenderModelError_None) return null;
        RenderModel renderModel = new RenderModel(modelPointer.getByteBuffer(RenderModel.SIZEOF));

        error = 0;
        PointerBuffer texturePointer = PointerBuffer.allocateDirect(1);
        while (true) {
            error = VRRenderModels.VRRenderModels_LoadTexture_Async(renderModel.diffuseTextureId(), texturePointer);
            if (error != VR.EVRRenderModelError_VRRenderModelError_Loading) break;
        }

        if (error != VR.EVRRenderModelError_VRRenderModelError_None) {
            VRRenderModels.VRRenderModels_FreeRenderModel(renderModel);
            return null;
        }

        RenderModelTextureMap renderModelTexture = new RenderModelTextureMap(texturePointer.getByteBuffer(RenderModelTextureMap.SIZEOF));

        // convert to a Model
        Mesh mesh = new Mesh(true, renderModel.unVertexCount(), renderModel.unTriangleCount() * 3, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
        MeshPart meshPart = new MeshPart(name, mesh, 0, renderModel.unTriangleCount() * 3, GL20.GL_TRIANGLES);
        RenderModelVertex.Buffer vertices = renderModel.rVertexData();
        float[] packedVertices = new float[8 * renderModel.unVertexCount()];
        int i = 0;
        while(vertices.remaining() > 0) {
            RenderModelVertex v = vertices.get();
            packedVertices[i++] = v.vPosition().v(0);
            packedVertices[i++] = v.vPosition().v(1);
            packedVertices[i++] = v.vPosition().v(2);

            packedVertices[i++] = v.vNormal().v(0);
            packedVertices[i++] = v.vNormal().v(1);
            packedVertices[i++] = v.vNormal().v(2);

            packedVertices[i++] = v.rfTextureCoord().get(0);
            packedVertices[i++] = v.rfTextureCoord().get(1);
        }
        mesh.setVertices(packedVertices);
        short[] indices = new short[renderModel.unTriangleCount() * 3];
        renderModel.IndexData().get(indices);
        mesh.setIndices(indices);

        Pixmap pixmap = new Pixmap(renderModelTexture.unWidth(), renderModelTexture.unHeight(), Format.RGBA8888);
        byte[] pixels = new byte[renderModelTexture.unWidth() * renderModelTexture.unHeight() * 4];
        renderModelTexture.rubTextureMapData(pixels.length).get(pixels);
        pixmap.getPixels().put(pixels);
        pixmap.getPixels().position(0);
        Texture texture = new Texture(new PixmapTextureData(pixmap, pixmap.getFormat(), true, true));
        Material material = new Material(new TextureAttribute(TextureAttribute.Diffuse, texture));

        Model model = new Model();
        model.meshes.add(mesh);
        model.meshParts.add(meshPart);
        model.materials.add(material);
        Node node = new Node();
        node.id = name;
        node.parts.add(new NodePart(meshPart, material));
        model.nodes.add(node);
        model.manageDisposable(mesh);
        model.manageDisposable(texture);

        VRRenderModels.VRRenderModels_FreeRenderModel(renderModel);
        VRRenderModels.VRRenderModels_FreeTexture(renderModelTexture);

        models.put(name, model);

        return model;
    }

    /**
     * Keeps track of per eye data such as rendering surface,
     * or {@link VRCamera}.
     */
    public static class VRPerEyeData {
        /** the {@link GLFrameBuffer<Texture>} for this eye */
        private GLFrameBuffer<Texture> buffer;
        /** a {@link TextureRegion} wrapping the color texture of the framebuffer for 2D rendering **/
        public final TextureRegion region;
        /** the {@link VRCamera} for this eye **/
        public final VRCamera camera;
        /** used internally to submit the frame buffer to OpenVR **/
        final org.lwjgl.openvr.Texture texture;

        VRPerEyeData(FrameBuffer buffer, TextureRegion region, VRCamera cameras) {
            this.buffer = buffer;
            this.region = region;
            this.camera = cameras;
            this.texture = org.lwjgl.openvr.Texture.create();
            this.texture.set(buffer.getColorBufferTexture().getTextureObjectHandle(), VR.ETextureType_TextureType_OpenGL, VR.EColorSpace_ColorSpace_Gamma);
        }

        public void setFrameBuffer(GLFrameBuffer<Texture> fbo, boolean disposeOld) {
            if (disposeOld) {
                this.buffer.dispose();
            }
            this.buffer = fbo;
            this.region.setTexture(fbo.getColorBufferTexture());
            this.texture.set(buffer.getColorBufferTexture().getTextureObjectHandle(), VR.ETextureType_TextureType_OpenGL, VR.EColorSpace_ColorSpace_Gamma);
        }

        public GLFrameBuffer<Texture> getFrameBuffer() {
            return buffer;
        }
    }

    /**
     * Represents the pose of a {@link VRDevice}, including its
     * transform, velocity and angular velocity. Also indicates
     * whether the pose is valid and whether the device is connected.
     */
    public static class VRDevicePose {
        /** transform encoding the position and rotation of the device in tracker space **/
        public final Matrix4 transform = new Matrix4();
        /** the velocity in m/s in tracker space space **/
        public final Vector3 velocity = new Vector3();
        /** the angular velocity in radians/s in tracker space **/
        public final Vector3 angularVelocity = new Vector3();
        /** whether the pose is valid our invalid, e.g. outdated because of tracking failure**/
        public boolean isValid;
        /** whether the device is connected **/
        public boolean isConnected;
        /** the device index **/
        private final int index;

        public VRDevicePose(int index) {
            this.index = index;
        }
    }

    /**
     * Represents a tracked VR device such as the head mounted
     * display, wands etc.
     */
    public class VRDevice {
        private final VRDevicePose pose;
        private final VRDeviceType type;
        private VRControllerRole role;
        private long buttons = 0;
        private final VRControllerState state = VRControllerState.create();
        private final ModelInstance modelInstance;

        // tracker space
        private final Vector3 position = new Vector3();
        private final Vector3 xAxis = new Vector3();
        private final Vector3 yAxis = new Vector3();
        private final Vector3 zAxis = new Vector3();

        // world space
        private final Vector3 positionWorld = new Vector3();
        private final Vector3 xAxisWorld = new Vector3();
        private final Vector3 yAxisWorld = new Vector3();
        private final Vector3 zAxisWorld = new Vector3();

        private final Vector3 vecTmp = new Vector3();
        private final Matrix4 matTmp = new Matrix4();

        VRDevice(VRDevicePose pose, VRDeviceType type, VRControllerRole role) {
            this.pose = pose;
            this.type = type;
            this.role = role;
            Model model = loadRenderModel(getStringProperty(VRDeviceProperty.RenderModelName_String));
            this.modelInstance = model != null ? new ModelInstance(model) : null;
            if (model != null) this.modelInstance.transform.set(pose.transform);
        }

        /**
         * @return the most up-to-date {@link VRDevicePose} in tracker space
         */
        public VRDevicePose getPose() {
            return pose;
        }

        public void updateAxesAndPosition() {
            Matrix4 matrix = pose.transform;
            matrix.getTranslation(position);
            xAxis.set(matrix.val[Matrix4.M00], matrix.val[Matrix4.M10], matrix.val[Matrix4.M20]).nor();
            yAxis.set(matrix.val[Matrix4.M01], matrix.val[Matrix4.M11], matrix.val[Matrix4.M21]).nor();
            zAxis.set(matrix.val[Matrix4.M02], matrix.val[Matrix4.M12], matrix.val[Matrix4.M22]).nor().scl(-1);

            matTmp.set(trackerSpaceToWorldspaceRotationOffset);
            positionWorld.set(position).mul(matTmp);
            positionWorld.add(trackerSpaceOriginToWorldSpaceTranslationOffset);

            matTmp.set(trackerSpaceToWorldspaceRotationOffset);

            xAxisWorld.set(xAxis).mul(matTmp);
            yAxisWorld.set(yAxis).mul(matTmp);
            zAxisWorld.set(zAxis).mul(matTmp);
        }

        /**
         * @return the position in the given {@link Space}
         */
        public Vector3 getPosition(Space space) {
            return space == Space.Tracker ? position : positionWorld;
        }

        /**
         * @return the right vector in the given {@link Space}
         */
        public Vector3 getRight(Space space) {
            return space == Space.Tracker ? xAxis : xAxisWorld;
        }

        /**
         * @return the up vector in the given {@link Space}
         */
        public Vector3 getUp(Space space) {
            return space == Space.Tracker ? yAxis : yAxisWorld;
        }

        /**
         * @return the direction vector in the given {@link Space}
         */
        public Vector3 getDirection(Space space) {
            return space == Space.Tracker ? zAxis : zAxisWorld;
        }

        /**
         * @return the {@link VRDeviceType}
         */
        public VRDeviceType getType() {
            return type;
        }

        /**
         * The {@link VRControllerRole}, indicating if the {@link VRDevice} is assigned
         * to the left or right hand.
         *
         * <p>
         * <strong>Note</strong>: the role is not reliable! If one controller is connected on
         * startup, it will have a role of {@link VRControllerRole#Unknown} and retain
         * that role even if a second controller is connected (which will also haven an
         * unknown role). The role is only reliable if two controllers are connected
         * already, and none of the controllers disconnects during the application
         * life-time.</br>
         * At least on the HTC Vive, the first connected controller is always the right hand
         * and the second connected controller is the left hand. The order stays the same
         * even if controllers disconnect/reconnect during the application life-time.
         * </p>
         */
        // FIXME role might change as per API, but never saw it
        public VRControllerRole getControllerRole() {
            return role;
        }

        /**
         * @return whether the device is connected
         */
        public boolean isConnected() {
            return VRSystem.VRSystem_IsTrackedDeviceConnected(pose.index);
        }

        /**
         * @return whether the button from {@link VRControllerButtons} is pressed
         */
        public boolean isButtonPressed(int button) {
            if (button < 0 || button >= 64) return false;
            return (buttons & (1l << button)) != 0;
        }

        void setButton(int button, boolean pressed) {
            if (pressed) {
                buttons |= (1l << button);
            } else {
                buttons ^= (1l << button);
            }
        }

        /**
         * @return the x-coordinate in the range [-1, 1] of the given axis from {@link VRControllerAxes}
         */
        public float getAxisX(int axis) {
            if (axis < 0 || axis >= 5) return 0;
            VRSystem.VRSystem_GetControllerState(pose.index, state);
            return state.rAxis(axis).x();
        }

        /**
         * @return the y-coordinate in the range [-1, 1] of the given axis from {@link VRControllerAxes}
         */
        public float getAxisY(int axis) {
            if (axis < 0 || axis >= 5) return 0;
            VRSystem.VRSystem_GetControllerState(pose.index, state);
            return state.rAxis(axis).y();
        }

        /**
         * Trigger a haptic pulse (vibrate) for the duration in microseconds. Subsequent calls
         * to this method within 5ms will be ignored.
         * @param duration pulse duration in microseconds
         */
        public void triggerHapticPulse(short duration) {
            VRSystem.VRSystem_TriggerHapticPulse(pose.index, 0, duration);
        }

        /**
         * @return a boolean property or false if the query failed
         */
        public boolean getBooleanProperty(VRDeviceProperty property) {
            scratch.put(0, 0);
            boolean result = VRSystem.VRSystem_GetBoolTrackedDeviceProperty(this.pose.index, property.value, scratch);
            if (scratch.get(0) != 0) return false;
            else return result;
        }

        /**
         * @return a float property or 0 if the query failed
         */
        public float getFloatProperty(VRDeviceProperty property) {
            scratch.put(0, 0);
            float result = VRSystem.VRSystem_GetFloatTrackedDeviceProperty(this.pose.index, property.value, scratch);
            if (scratch.get(0) != 0) return 0;
            else return result;
        }

        /**
         * @return an int property or 0 if the query failed
         */
        public int getInt32Property(VRDeviceProperty property) {
            scratch.put(0, 0);
            int result = VRSystem.VRSystem_GetInt32TrackedDeviceProperty(this.pose.index, property.value, scratch);
            if (scratch.get(0) != 0) return 0;
            else return result;
        }

        /**
         * @return a long property or 0 if the query failed
         */
        public long getUInt64Property(VRDeviceProperty property) {
            scratch.put(0, 0);
            long result = VRSystem.VRSystem_GetUint64TrackedDeviceProperty(this.pose.index, property.value, scratch);
            if (scratch.get(0) != 0) return 0;
            else return result;
        }

        /**
         * @return a string property or null if the query failed
         */
        public String getStringProperty(VRDeviceProperty property) {
            scratch.put(0, 0);

            String result = VRSystem.VRSystem_GetStringTrackedDeviceProperty(this.pose.index, property.value, scratch);
            if (scratch.get(0) != 0) return null;
            return result;
        }

        /**
         * @return a {@link ModelInstance} with the transform updated to the latest tracked position and orientation in world space for rendering or null
         */
        public ModelInstance getModelInstance() {
            return modelInstance;
        }

        @Override
        public String toString() {
            return "VRDevice[manufacturer=" + getStringProperty(VRDeviceProperty.ManufacturerName_String) + ", renderModel=" + getStringProperty(VRDeviceProperty.RenderModelName_String) + ", index=" + pose.index + ", type=" + type + ", role=" + role + "]";
        }
    }

    public interface VRDeviceListener {
        /** A new {@link VRDevice} has connected **/
        void connected(VRDevice device);

        /** A {@link VRDevice} has disconnected **/
        void disconnected(VRDevice device);

        /** A button from {@link VRControllerButtons} was pressed on the {@link VRDevice} **/
        void buttonPressed(VRDevice device, int button);

        /** A button from {@link VRControllerButtons} was released on the {@link VRDevice} **/
        void buttonReleased(VRDevice device, int button);
    }

    static void hmdMat4toMatrix4(HmdMatrix44 hdm, Matrix4 mat) {
        float[] val = mat.val;
        FloatBuffer m = hdm.m();

        val[0] = m.get(0);
        val[1] = m.get(4);
        val[2] = m.get(8);
        val[3] = m.get(12);

        val[4] = m.get(1);
        val[5] = m.get(5);
        val[6] = m.get(9);
        val[7] = m.get(13);

        val[8] = m.get(2);
        val[9] = m.get(6);
        val[10] = m.get(10);
        val[11] = m.get(14);

        val[12] = m.get(3);
        val[13] = m.get(7);
        val[14] = m.get(11);
        val[15] = m.get(15);
    }

    static void hmdMat34ToMatrix4(HmdMatrix34 hmd, Matrix4 mat) {
        float[] val = mat.val;
        FloatBuffer m = hmd.m();

        val[0] = m.get(0);
        val[1] = m.get(4);
        val[2] = m.get(8);
        val[3] = 0;

        val[4] = m.get(1);
        val[5] = m.get(5);
        val[6] = m.get(9);
        val[7] = 0;

        val[8] = m.get(2);
        val[9] = m.get(6);
        val[10] = m.get(10);
        val[11] = 0;

        val[12] = m.get(3);
        val[13] = m.get(7);
        val[14] = m.get(11);
        val[15] = 1;
    }
}