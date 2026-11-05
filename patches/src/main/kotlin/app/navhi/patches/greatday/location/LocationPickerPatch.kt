package app.navhi.patches.greatday.location

import app.navhi.patches.greatday.shared.Constants.COMPATIBILITY_GREATDAY
import app.navhi.patches.greatday.shared.addMethod
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation

private const val ACTIVITY = "Lcom/senjuid/location/GeolocationActivity;"
private const val ACTIVITY_ONE = "Lcom/senjuid/location/GeolocationActivity\$1;"
private const val ACTIVITY_FOUR = "Lcom/senjuid/location/GeolocationActivity\$4;"
private const val ACTIVITY_SEVEN = "Lcom/senjuid/location/GeolocationActivity\$7;"
private const val VIEW = "Landroid/view/View;"
private const val GOOGLE_MAP = "Lcom/google/android/gms/maps/GoogleMap;"
private const val CAMERA_UPDATE = "Lcom/google/android/gms/maps/CameraUpdate;"
private const val LOCATION = "Landroid/location/Location;"
private const val LAT_LNG = "Lcom/google/android/gms/maps/model/LatLng;"
private const val PICKER = "Lapp/navhi/extension/greatday/LocationPicker;"
private const val STORE = "Lapp/navhi/extension/greatday/LocationStore;"

/** Map is ready: the activity field is populated and the camera has been moved. */
private val mapReadyFingerprint = Fingerprint(
    definingClass = "$ACTIVITY_ONE",
    name = "onMapReady",
    parameters = listOf(GOOGLE_MAP),
    filters = listOf(
        methodCall(ACTIVITY, "a", listOf(ACTIVITY, GOOGLE_MAP), GOOGLE_MAP),
        methodCall("Lcom/google/android/gms/maps/GoogleMap;", "moveCamera", listOf(CAMERA_UPDATE), "V"),
    ),
)

/** The locate-me button. */
private val recentreFingerprint = Fingerprint(
    definingClass = "$ACTIVITY_FOUR",
    name = "onClick",
    parameters = listOf(VIEW),
    filters = listOf(
        methodCall(ACTIVITY, "b", listOf(ACTIVITY), "V"),
    ),
)

/** The confirm button. */
private val confirmFingerprint = Fingerprint(
    definingClass = "$ACTIVITY_SEVEN",
    name = "onClick",
    parameters = listOf(VIEW),
)

/** Live fixes: keep the camera on the pin while one is dropped. */
private val liveFixFingerprint = Fingerprint(
    definingClass = ACTIVITY,
    name = "a",
    parameters = listOf(LOCATION),
    returnType = "V",
    filters = listOf(
        methodCall("Lcom/google/android/gms/maps/GoogleMap;", "setMyLocationEnabled", listOf("Z"), "V"),
    ),
)

/*
 * The stock screen already has a map plus commit and recentre buttons; this
 * turns it into a pin picker over the extension's {@link LocationPicker}.
 */
@Suppress("unused")
val locationPickerPatch = bytecodePatch(
    name = "Location picker screen",
    description = "Drop a pin on the app's map screen; confirm saves it, recentre goes back to live GPS.",
) {
    compatibleWith(COMPATIBILITY_GREATDAY)

    dependsOn(manualLocationPatch)

    execute {
        val activity = mutableClassDefBy(ACTIVITY)

        activity.addMethod(
            "z",
            emptyList(),
            "V",
            0,
            """
                invoke-static {}, $PICKER->refresh()V
                return-void
            """,
        )

        activity.addMethod(
            "p",
            emptyList(),
            "V",
            2,
            """
                sget-object v0, $PICKER->map:$GOOGLE_MAP
                if-nez v0, :end
                invoke-static { v0, p0 }, $PICKER->listen(${GOOGLE_MAP}Ljava/lang/Object;)V
                invoke-static { p0 }, $PICKER->savedPin(Landroid/content/Context;)$LAT_LNG
                move-result-object v0
                if-nez v0, :end
                invoke-static { v0 }, $PICKER->pin($LAT_LNG)V
                const/4 v1, 0x0
                invoke-static { v0, v1 }, $PICKER->move(${LAT_LNG}Z)V
                invoke-direct { p0 }, $ACTIVITY->e()V
                :end
                return-void
            """,
        )

        activity.addMethod(
            "q",
            listOf(LAT_LNG),
            "V",
            0,
            """
                invoke-static { p1 }, $PICKER->pin($LAT_LNG)V
                invoke-direct { p0 }, $ACTIVITY->e()V
                return-void
            """,
        )

        activity.addMethod(
            "r",
            emptyList(),
            "V",
            0,
            """
                invoke-static {}, $PICKER->centre()V
                invoke-direct { p0 }, $ACTIVITY->e()V
                return-void
            """,
        )

        activity.addMethod(
            "s",
            emptyList(),
            "V",
            0,
            """
                invoke-static { p0 }, $STORE->clear(Landroid/content/Context;)V
                invoke-static {}, $PICKER->clear()V
                invoke-direct { p0 }, $ACTIVITY->e()V
                return-void
            """,
        )

        activity.addMethod(
            "u",
            listOf(LAT_LNG),
            "V",
            1,
            """
                const/4 v0, 0x1
                invoke-static { p1, v0 }, $PICKER->move(${LAT_LNG}Z)V
                return-void
            """,
        )

        activity.addMethod(
            "v",
            emptyList(),
            "V",
            8,
            """
                sget-object v0, $PICKER->pin:$LAT_LNG
                if-eqz v0, :pinned
                iget-wide v4, v0, $LAT_LNG->latitude:D
                iget-wide v6, v0, $LAT_LNG->longitude:D
                goto :save
                :pinned
                iget-object v0, p0, $ACTIVITY->p:Lcom/senjuid/location/b;
                iget-object v0, v0, Lcom/senjuid/location/b;->b:Landroidx/lifecycle/MutableLiveData;
                invoke-virtual { v0 }, Landroidx/lifecycle/MutableLiveData;->getValue()Ljava/lang/Object;
                move-result-object v0
                check-cast v0, $LOCATION
                if-nez v0, :end
                invoke-virtual { v0 }, $LOCATION->getLatitude()D
                move-result-wide v4
                invoke-virtual { v0 }, $LOCATION->getLongitude()D
                move-result-wide v6
                :save
                invoke-static { p0, v4, v5, v6, v7 }, $STORE->save(Landroid/content/Context;DD)V
                new-instance v0, $LOCATION
                const-string v1, "gps"
                invoke-direct { v0, v1 }, $LOCATION-><init>(Ljava/lang/String;)V
                invoke-virtual { v0, v4, v5 }, $LOCATION->setLatitude(D)V
                invoke-virtual { v0, v6, v7 }, $LOCATION->setLongitude(D)V
                invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
                move-result-wide v1
                invoke-virtual { v0, v1, v2 }, $LOCATION->setTime(J)V
                invoke-direct { p0, v0 }, $ACTIVITY->b($LOCATION)V
                :end
                return-void
            """,
        )

        mapReadyFingerprint.let { fingerprint ->
            val method = fingerprint.method
            val index = fingerprint.instructionMatches.first().index
            method.addInstructions(
                index + 1,
                """
                    iget-object v1, p0, $ACTIVITY_ONE->a:$ACTIVITY
                    invoke-virtual { v1 }, $ACTIVITY->p()V
                """,
            )
        }

        recentreFingerprint.let { fingerprint ->
            val method = fingerprint.method
            method.addInstructions(
                fingerprint.instructionMatches.first().index,
                """
                    iget-object v0, p0, $ACTIVITY_FOUR->a:$ACTIVITY
                    invoke-virtual { v0 }, $ACTIVITY->s()V
                """,
            )
        }

        confirmFingerprint.let { fingerprint ->
            val method = fingerprint.method
            method.removeInstructions(0, method.implementation!!.instructions.count())
            method.addInstructions(
                0,
                """
                    iget-object p0, p0, $ACTIVITY_SEVEN->a:$ACTIVITY
                    invoke-virtual { p0 }, $ACTIVITY->v()V
                    return-void
                """,
            )
        }

        liveFixFingerprint.let { fingerprint ->
            val method = fingerprint.method
            method.addInstructions(
                fingerprint.instructionMatches.first().index,
                """
                    sget-object v2, $PICKER->pin:$LAT_LNG
                    if-eqz v2, :no_pin
                    const/4 v3, 0x0
                    invoke-static { v2, v3 }, $PICKER->move(${LAT_LNG}Z)V
                    :no_pin
                """,
            )
        }
    }
}
