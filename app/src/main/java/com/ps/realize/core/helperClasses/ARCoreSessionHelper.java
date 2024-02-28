package com.ps.realize.core.helperClasses;

import android.app.Activity;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;

public class ARCoreSessionHelper {

    public static Session createArCoreSession(Activity activity) {
        // Check if ARCore is installed and supported on this device
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(activity);
        if (availability == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
            // ARCore is not supported on this device
            // Handle unsupported device
            return null;
        }

        // Create a configuration for the ARCore session
//        Config config = new Config(Session.FeatureMapQuality.FEATURE_MAP_QUALITY_HIGH);
//        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);

        // Create the ARCore session
        Session session = null;
        try {
            session = new Session(activity);
        } catch (UnavailableArcoreNotInstalledException e) {
            throw new RuntimeException(e);
        } catch (UnavailableApkTooOldException e) {
            throw new RuntimeException(e);
        } catch (UnavailableSdkTooOldException e) {
            throw new RuntimeException(e);
        } catch (UnavailableDeviceNotCompatibleException e) {
            throw new RuntimeException(e);
        }
//        session.configure(config);

        return session;
    }
}
