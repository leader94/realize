package com.ps.realize.core.daos.user;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ps.realize.core.datamodels.User;

@Dao
public interface UserDao {
//    @Query("SELECT * FROM user")
//    List<User> getAll();

//    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
//    List<User> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM user WHERE id LIKE :id")
    User getUserById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(User... users);

    @Delete
    void delete(User user);
}