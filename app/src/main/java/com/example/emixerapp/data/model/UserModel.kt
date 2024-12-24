package com.example.emixerapp.data.model

import android.os.Parcel
import android.os.Parcelable
import java.util.UUID

/**
 * Data class representing a user in the application.  Implements Parcelable for efficient data passing
 * between Android components.
 */
data class UserModel(
    val id: UUID = UUID.randomUUID(), // Unique identifier for the user. Generated automatically if not provided.
    var name: String = "",           // User's name (mutable).
    var iconIndex: Int = 0,          // Index of the icon associated with the user (mutable).
    var bass: Int = 0,               // Bass level (mutable).
    var middle: Int = 0,             // Middle level (mutable).
    var high: Int = 0,              // High level (mutable).
    var mainVolume: Int = 50,        // Main volume level (mutable). Default value 50.
    var pan: Int = 50               // Pan level (mutable). Default value 50.
) : Parcelable {

    /**
     * Constructor used for creating a UserModel from a Parcel. Required for Parcelable implementation.
     */
    constructor(parcel: Parcel) : this(
        UUID.fromString(parcel.readString()), // Read the ID from the Parcel.
        parcel.readString() ?: "",           // Read the name from the Parcel.  Use ?: "" to handle potential nulls.
        parcel.readInt(),                    // Read the icon index from the Parcel.
        parcel.readInt(),                    // Read the bass level from the Parcel.
        parcel.readInt(),                    // Read the middle level from the Parcel.
        parcel.readInt(),                    // Read the high level from the Parcel.
        parcel.readInt(),                    // Read the main volume from the Parcel.
        parcel.readInt()                     // Read the pan level from the Parcel.
    )

    /**
     * Writes the contents of this UserModel to a Parcel. Required for Parcelable implementation.
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id.toString())     // Write the ID to the Parcel.
        parcel.writeString(name)              // Write the name to the Parcel.
        parcel.writeInt(iconIndex)            // Write the icon index to the Parcel.
        parcel.writeInt(bass)                 // Write the bass level to the Parcel.
        parcel.writeInt(middle)               // Write the middle level to the Parcel.
        parcel.writeInt(high)                 // Write the high level to the Parcel.
        parcel.writeInt(mainVolume)           // Write the main volume to the Parcel.
        parcel.writeInt(pan)                  // Write the pan level to the Parcel.
    }

    /**
     * Describes the contents of this UserModel. Required for Parcelable implementation.  Returns 0 for simplicity.
     */
    override fun describeContents(): Int {
        return 0
    }

    /**
     * Companion object containing the CREATOR, which is required for Parcelable implementation.
     * This allows Android to create new UserModel instances from Parcels.
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
