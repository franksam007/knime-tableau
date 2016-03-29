// -----------------------------------------------------------------------------
//
// This file is the copyrighted property of Tableau Software and is protected
// by registered patents and other applicable U.S. and international laws and
// regulations.
//
// Unlicensed use of the contents of this file is prohibited. Please refer to
// the NOTICES.txt file for further details.
//
// NOTE: This sample requires a C99 or higher compiler, i.e. Microsoft Visual 
// C compiler 2013 and above, and GCC with C99.
//
// -----------------------------------------------------------------------------
#if defined(__APPLE__) && defined(__MACH__)
#include <TableauServer/TableauServer.h>
#else
#include "TableauServer.h"
#endif

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

#define CreateTableauString(STR,NAME) \
    static const wchar_t NAME##_str[]=STR; \
    TableauWChar NAME[sizeof(NAME##_str)/sizeof(wchar_t)]; \
    ToTableauString( NAME##_str, NAME )

void TryOp(TAB_RESULT result) {
    // Handle the exception depending on the type of result received

    if(result == TAB_RESULT_Success) return; // It was successful so there is no error to handle

    const wchar_t* errorType = NULL;
    const wchar_t* message = NULL;

    if(result == TAB_RESULT_InternalError)
    {
        errorType = L"InternalError";
        message = L"Could not parse the response from the server.";
    }
    else if(result == TAB_RESULT_InvalidArgument)
    {
        errorType = L"InvalidArgument";
        message = TabGetLastErrorMessage();
    }
    else if(result == TAB_RESULT_CurlError)
    {
        errorType = L"CurlError";
        message = TabGetLastErrorMessage();
    }
    else if(result == TAB_RESULT_ServerError)
    {
        errorType = L"ServerError";
        message = TabGetLastErrorMessage();
    }
    else if(result == TAB_RESULT_NotAuthenticated)
    {
        errorType = L"NotAuthenticated";
        message = TabGetLastErrorMessage();
    }
    else if(result == TAB_RESULT_BadPayload)
    {
        errorType = L"BadPayload";
        message = L"Unknown response from the server. Make sure this version of Tableau API is compatible with your server.";
    }
    else if(result == TAB_RESULT_InitError)
    {
        errorType = L"InitError";
        message = TabGetLastErrorMessage();
    }
    else
    {
        errorType = L"UnknownError";
        message = L"An unknown error occured.";
    }

    fprintf(stderr, "Error: %ls - %ls\n", errorType, message);
    exit( EXIT_FAILURE );
}

int main( int argc, char* argv[] )
{
    TAB_HANDLE hServer;
    bool       bOverwrite = false;

    CreateTableauString( L"http://localhost", sHost );
    CreateTableauString( L"username", sUsername );
    CreateTableauString( L"password", sPassword );
    CreateTableauString( L"siteID", sSiteID );
    CreateTableauString( L"order-c.tde", sPath );
    CreateTableauString( L"default", sProjectName );
    CreateTableauString( L"Order-c", sDatasourceName );

    // Initialize Tableau Server API
    TryOp( TabServerAPIInitialize() );

    // Create the server connection
    TryOp( TabServerConnectionCreate( &hServer ) );

    // Connect to the server
    TryOp( TabServerConnectionConnect( hServer, sHost, sUsername, sPassword, sSiteID ) );

    // Publish order-c.tde to the server under the default project with name Order-c
    TryOp( TabServerConnectionPublishExtract( hServer, sPath, sProjectName, sDatasourceName, bOverwrite ) );

    // Disconnect from the server
    TryOp( TabServerConnectionDisconnect( hServer ) );

    // Destroy the server connection
    TryOp( TabServerConnectionClose( hServer ) );

    // Clean up Tableau Server API
    TryOp( TabServerAPICleanup() );

    return 0;
}
