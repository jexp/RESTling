package com.tinkerpop.restling.domain;

/**
 * Thrown if there's something wrong regarding property values/types
 */
public class PropertyValueException extends RuntimeException
{
    public PropertyValueException()
    {
        super();
    }

    public PropertyValueException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public PropertyValueException( String message )
    {
        super( message );
    }

    public PropertyValueException( Throwable cause )
    {
        super( cause );
    }
}
