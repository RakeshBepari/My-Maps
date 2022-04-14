package com.example.mymaps

import android.content.Context
import android.util.Log
import com.example.mymaps.models.UserMap
import java.io.*

const val FILENAME="UserMapDataFile"
class FileStoreUserMap(private val mcontext: Context) {
    private val TAG="FileStoreUserMap"

    fun deserializeUserMaps(): List<UserMap> {
        Log.i(TAG, "DeserializeUserMaps/Reading from a file")
        val dataFile = getDataFile(mcontext)
        if (!dataFile.exists()) {
            Log.i(TAG, "File does not exists yet")
            return emptyList()
        }
        ObjectInputStream(FileInputStream(dataFile)).use { return it.readObject() as List<UserMap>}
    }
    fun serializeUserMaps(userMaps: List<UserMap>){
        Log.i(TAG,"SerializeUserMaps/Writing to file")
        ObjectOutputStream(FileOutputStream(getDataFile(mcontext))).use {it.writeObject(userMaps) }
    }
    fun getDataFile(context: Context):File{
        Log.i(TAG,"Getting file from Directory ${context.filesDir}")
        return File(context.filesDir, FILENAME)
    }
}