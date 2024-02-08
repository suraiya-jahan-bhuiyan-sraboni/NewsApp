package com.example.newsapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.newsapp.models.Article

@Database(entities = [Article::class], version = 2, exportSchema = true)
@TypeConverters(Converters::class)
abstract class ArticleDatabase:RoomDatabase(){
     abstract fun getArticleDao():ArticleDao
    companion object{
        @Volatile
        private var INSTANCE:ArticleDatabase?=null
         fun getInstance(context: Context):ArticleDatabase = synchronized(this){
            var instance= INSTANCE
            if(instance==null){
                instance= Room.databaseBuilder(
                    context.applicationContext,ArticleDatabase::class.java,"articles"
                ).fallbackToDestructiveMigration().build()
            }
             INSTANCE =instance
             return instance
         }
    }
}