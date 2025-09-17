package com.rollinup.server

object ExpiredTokenExceptions : Exception("Error, Token was Expired") {
    private fun readResolve(): Any = ExpiredTokenExceptions
}