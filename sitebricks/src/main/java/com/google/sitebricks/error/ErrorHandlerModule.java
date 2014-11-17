package com.google.sitebricks.error;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class ErrorHandlerModule extends AbstractModule {

    @Override
    protected final void configure() {
        bind(ErrorHandler.class).to(ErrorHandlerImpl.class).in(Singleton.class);
    }
}
