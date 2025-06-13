package com.example.tea3_blais.model

import java.io.Serializable

data class ProfilListeToDo(
    private var login: String,
    private val mesListeToDo: MutableList<ListeToDo> = mutableListOf()
) : Serializable {

    fun getLogin(): String = login

    fun setLogin(unLogin: String) {
        login = unLogin
    }

    fun getMesListeToDo(): List<ListeToDo> = mesListeToDo

    fun setMesListeToDo(listes: List<ListeToDo>) {
        mesListeToDo.clear()
        mesListeToDo.addAll(listes)
    }

    fun ajouteListe(uneListe: ListeToDo) {
        mesListeToDo.add(uneListe)
    }

    override fun toString(): String {
        return login
    }
} 