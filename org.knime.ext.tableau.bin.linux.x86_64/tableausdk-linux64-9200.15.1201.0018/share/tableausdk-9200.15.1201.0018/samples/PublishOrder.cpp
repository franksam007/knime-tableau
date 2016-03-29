// -----------------------------------------------------------------------------
//
// This file is the copyrighted property of Tableau Software and is protected
// by registered patents and other applicable U.S. and international laws and
// regulations.
//
// Unlicensed use of the contents of this file is prohibited. Please refer to
// the NOTICES.txt file for further details.
//
// -----------------------------------------------------------------------------
#if defined(__APPLE__) && defined(__MACH__)
#include <TableauServer/TableauServer_cpp.h>
#else
#include "TableauServer_cpp.h"
#endif

#include <stdlib.h>
#include <iostream>

using namespace Tableau;

int main( int argc, char* argv[] )
{
    try {
        // Initialize Tableau Server API
        ServerAPI::Initialize();

        // Create the server connection object
        ServerConnection serverConnection;

        // Connect to the server
        serverConnection.Connect( L"http://localhost", L"username", L"password", L"siteID" );

        // Publish order-cpp.tde to the server under the default project with name Order-cpp
        serverConnection.PublishExtract( L"order-cpp.tde", L"default", L"Order-cpp", false );

        // Disconnect from the server
        serverConnection.Disconnect();

        // Destroy the server connection object
        serverConnection.Close();

        // Clean up Tableau Server API
        ServerAPI::Cleanup();
    }
    catch ( const TableauException& e) {
        // Handle the exception depending on the type of exception received

        std::wcerr << L"Error: ";

        switch(e.GetResultCode())
        {
        case Result::Result_InternalError:
            std::wcerr << L"InternalError - Could not parse the response from the server." << std::endl;
            break;
        case Result::Result_InvalidArgument:
            std::wcerr << L"InvalidArgument - " << e.GetMessage() << std::endl;
            break;
        case Result::Result_CurlError:
            std::wcerr << L"CurlError - " << e.GetMessage() << std::endl;
            break;
        case Result::Result_ServerError:
            std::wcerr << L"ServerError - " << e.GetMessage() << std::endl;
            break;
        case Result::Result_NotAuthenticated:
            std::wcerr << L"NotAuthenticated - " << e.GetMessage() << std::endl;
            break;
        case Result::Result_BadPayload:
            std::wcerr << L"BadPayload - Unknown response from the server. Make sure this version of Tableau API is compatible with your server." << std::endl;
            break;
        case Result::Result_InitError:
            std::wcerr << L"InitError - " << e.GetMessage() << std::endl;
            break;
        case Result::Result_UnknownError:
        default:
            std::wcerr << L"An unknown error occured." << std::endl;
        }

        exit( EXIT_FAILURE );
    }

    return 0;
}
