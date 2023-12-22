/*
 * Created by Injoit on 24.3.2023.
 * Copyright © 2023 Quickblox. All rights reserved.
 */

package com.quickblox.qb_qmunicate.domain.use_case.base

import kotlinx.coroutines.flow.Flow

abstract class FlowUseCase<Args, Result> : BaseUseCase<Args, Flow<Result>>()