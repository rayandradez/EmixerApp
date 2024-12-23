package com.example.emixerapp.data.model

import android.os.Parcel
import android.os.Parcelable
import java.util.UUID

/**
 * Data class representing a user in the application.  Implements Parcelable for efficient data passing
 * between Android components.
 *
 * @param id A unique identifier for the user (generated using UUID).
 * @param name The user's name.
 * @param iconIndex The index of the icon associated with the user.
 * @param bass The bass level (likely for audio settings).
 * @param middle The middle level (likely for audio settings).
 * @param truble The treble level (likely for audio settings).
 */
data class UserModel(
    val id: UUID = UUID.randomUUID(), // Unique identifier for the user.  Generated automatically if not provided.
    var name: String = "",           // User's name (mutable).
    var iconIndex: Int = 0,          // Index of the user's icon (mutable).
    val bass: Int = 0,               // Bass level (immutable).
    val middle: Int = 0,             // Middle level (immutable).
    val truble: Int = 0              // Treble level (immutable).
) : Parcelable {

    /**
     * Constructor used for creating a UserModel from a Parcel.  Required for Parcelable implementation.
     */
    constructor(parcel: Parcel) : this(
        UUID.fromString(parcel.readString()), // Read the ID from the Parcel.
        parcel.readString() ?: "",           // Read the name from the Parcel.
        parcel.readInt(),                    // Read the icon index from the Parcel.
        parcel.readInt(),                    // Read the bass level from the Parcel.
        parcel.readInt(),                    // Read the middle level from the Parcel.
        parcel.readInt()                     // Read the treble level from the Parcel.
    )

    /**
     * Writes the contents of this UserModel to a Parcel.  Required for Parcelable implementation.
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id.toString())     // Write the ID to the Parcel.
        parcel.writeString(name)              // Write the name to the Parcel.
        parcel.writeInt(iconIndex)            // Write the icon index to the Parcel.
        parcel.writeInt(bass)                 // Write the bass level to the Parcel.
        parcel.writeInt(middle)               // Write the middle level to the Parcel.
        parcel.writeInt(truble)               // Write the treble level to the Parcel.
    }

    /**
     * Describes the contents of this UserModel.  Required for Parcelable implementation.
     */
    override fun describeContents(): Int {
        return 0
    }

    /**
     * Companion object containing the CREATOR, which is required for Parcelable implementation.  This allows
     * Android to create new UserModel instances from Parcels.
     */
    companion object CREATOR : Parcelable.Creator<UserModel> {
        override fun createFromParcel(parcel: Parcel): UserModel {
            return UserModel(parcel)
        }

        override fun newArray(size: Int): Array<UserModel?> {
            return arrayOfNulls(size)
        }
    }
}
