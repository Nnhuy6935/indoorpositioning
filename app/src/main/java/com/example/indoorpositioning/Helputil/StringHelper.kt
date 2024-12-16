package com.example.indoorpositioning.Helputil
import com.example.indoorpositioning.Model.ObjectType

class StringHelper {
    fun createRandomVerticeId() : String{           //  random Id gồm 8 ký tự ngẫu nhiên
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..8)
            .map { allowedChars.random() }
            .joinToString("")
    }
    fun createRandomObjectId() : String{      // random id gồm tên object + 8 ký tự ngẫu nhiên
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return "OBJECT" + (1..8)
            .map { allowedChars.random() }
            .joinToString("")
    }
}