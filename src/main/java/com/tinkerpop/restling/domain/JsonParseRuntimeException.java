package com.tinkerpop.restling.domain;

public class JsonParseRuntimeException extends PropertyValueException
{
    public JsonParseRuntimeException()
    {
        super();
    }

    public JsonParseRuntimeException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public JsonParseRuntimeException( String message )
    {
        super( message );
    }

    public JsonParseRuntimeException( Throwable cause )
    {
        super( cause );
    }
}
