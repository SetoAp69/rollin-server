package com.rollinup.server

object InvalidTokenExceptions : Exception("Error, Token was Expired") {
    private fun readResolve(): Any = InvalidTokenExceptions
}

class CommonException(message: String) : Exception(message)

class UnauthorizedTokenException() : Exception()

class InvalidSettingException() : Exception()

class IllegalRoleException() : Exception()

class IllegalPathParameterException(path: String) : Exception(path)
