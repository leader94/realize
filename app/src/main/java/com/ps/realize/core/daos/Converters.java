package com.ps.realize.core.daos;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ps.realize.core.datamodels.json.ProjectObj;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Converters {
    @TypeConverter
    public static ArrayList<ProjectObj> fromString(String value) {
        Type listType = new TypeToken<ArrayList<ProjectObj>>() {
        }.getType();
        return new Gson().fromJson(value, listType);

    }

    @TypeConverter
    public static String fromArrayList(ArrayList<ProjectObj> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}