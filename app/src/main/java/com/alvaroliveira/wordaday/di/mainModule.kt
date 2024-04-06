package com.alvaroliveira.wordaday.di

import com.alvaroliveira.wordaday.usecase.GetWordsUseCase
import com.alvaroliveira.wordaday.presentation.MainViewModel
import com.prof18.rssparser.RssParser
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val mainModule: Module = module {
    singleOf(::RssParser)

    singleOf(::GetWordsUseCase)

    viewModelOf(::MainViewModel)

}