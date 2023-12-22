package com.quickblox.qb_qmunicate.domain.exception

const val DOMAIN_UNEXPECTED_EXCEPTION = "unexpected exception"

class DomainException(description: String) : Exception(description)