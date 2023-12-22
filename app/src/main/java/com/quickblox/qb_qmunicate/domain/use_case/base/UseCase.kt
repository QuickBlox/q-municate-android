/*
 * Created by Injoit on 13.01.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */
package com.quickblox.qb_qmunicate.domain.use_case.base

interface UseCase<TArgs, TResult> {
    suspend fun execute(args: TArgs): TResult
}