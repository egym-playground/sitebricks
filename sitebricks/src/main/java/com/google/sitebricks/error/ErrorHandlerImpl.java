package com.google.sitebricks.error;

class ErrorHandlerImpl implements ErrorHandler {

    @Override
    public void handleException(Exception ex) throws RuntimeException {
        if(ex instanceof RuntimeException){
            throw (RuntimeException) ex;
        }

        throw new RuntimeException(ex);
    }
}
