package com.quickblox.qb_qmunicate.domain.exception

class RepositoryException(description: String) : Exception(description) {
//TODO: add fabric method to create RepositoryException with different types of exceptions

    enum class Types {
        USER_ALREADY_EXIST,
        UNEXPECTED
    }
}