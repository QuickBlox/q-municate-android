/*
 * Created by Injoit on 7.4.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.qb_qmunicate.domain.use_case.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

abstract class BaseUseCase<Args, Result> : UseCase<Args, Result> {
    protected fun isScopeNotActive(scope: CoroutineScope): Boolean {
        return !scope.isActive
    }
}