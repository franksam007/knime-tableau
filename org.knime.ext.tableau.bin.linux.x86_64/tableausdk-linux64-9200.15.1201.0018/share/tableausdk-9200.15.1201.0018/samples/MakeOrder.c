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
#include <TableauExtract/TableauExtract.h>
#else
#include "TableauExtract.h"
#endif

#include <stdio.h>
#include <stdlib.h>

#define TryOp(x) if (x != TAB_RESULT_Success) { \
    fprintf(stderr, "Error: %ls\n", TabGetLastErrorMessage()); \
    exit( EXIT_FAILURE ); }

#define CreateTableauString(STR,NAME) \
    static const wchar_t NAME##_str[]=STR; \
    TableauWChar NAME[sizeof(NAME##_str)/sizeof(wchar_t)]; \
    ToTableauString( NAME##_str, NAME )

/* Define the table's schema */
TAB_HANDLE MakeTableDefinition()
{
    TAB_HANDLE hTableDef;

    CreateTableauString( L"Purchased", sPurchased );
    CreateTableauString( L"Product", sProduct );
    CreateTableauString( L"uProduct", sUProduct );
    CreateTableauString( L"Price", sPrice );
    CreateTableauString( L"Quantity", sQuantity );
    CreateTableauString( L"Taxed", sTaxed );
    CreateTableauString( L"Expiration Date", sExpirationDate );
    CreateTableauString( L"Produkt", sProdukt );

    TryOp( TabTableDefinitionCreate( &hTableDef ) );
    TryOp( TabTableDefinitionSetDefaultCollation( hTableDef, TAB_COLLATION_en_GB ) );
    TryOp( TabTableDefinitionAddColumn( hTableDef, sPurchased,      TAB_TYPE_DateTime ) );
    TryOp( TabTableDefinitionAddColumn( hTableDef, sProduct,        TAB_TYPE_CharString ) );
    TryOp( TabTableDefinitionAddColumn( hTableDef, sUProduct,       TAB_TYPE_UnicodeString ) );
    TryOp( TabTableDefinitionAddColumn( hTableDef, sPrice,          TAB_TYPE_Double ) );
    TryOp( TabTableDefinitionAddColumn( hTableDef, sQuantity,       TAB_TYPE_Integer ) );
    TryOp( TabTableDefinitionAddColumn( hTableDef, sTaxed,          TAB_TYPE_Boolean ) );
    TryOp( TabTableDefinitionAddColumn( hTableDef, sExpirationDate, TAB_TYPE_Date ) );

    /* Override Default collation */
    TryOp( TabTableDefinitionAddColumnWithCollation( hTableDef, sProdukt, TAB_TYPE_CharString, TAB_COLLATION_de ) );

    return hTableDef;
}

/* Print a Table's schema to stderr */
void PrintTableDefinition( TAB_HANDLE hTableDef )
{
    int i, numColumns, len;
    TAB_TYPE type;
    TableauString str;
    wchar_t* wStr;

    TryOp( TabTableDefinitionGetColumnCount( hTableDef, &numColumns ) );
    for ( i = 0; i < numColumns; ++i ) {
        TryOp( TabTableDefinitionGetColumnType( hTableDef, i, &type ) );
        TryOp( TabTableDefinitionGetColumnName( hTableDef, i, &str ) );

        len = TableauStringLength( str );
        wStr = (wchar_t*) malloc( (len + 1) * sizeof(wchar_t) ); /* make room for the null */
        FromTableauString( str, wStr );

        fprintf(stderr, "Column %d: %ls (%#06x)\n", i, wStr, type);
        free(wStr);
    }
}

/* Insert a few rows of data. */
void InsertData( TAB_HANDLE hTable )
{
    TAB_HANDLE hRow;
    TAB_HANDLE hTableDef;
    int i;

    CreateTableauString( L"uniBeans", sUniBeans );

    TryOp( TabTableGetTableDefinition( hTable, &hTableDef ) );

   /* Create a row to insert data */
    TryOp( TabRowCreate( &hRow, hTableDef ) );

    TryOp( TabRowSetDateTime(   hRow, 0, 2012, 7, 3, 11, 40, 12, 4550 ) ); /* Purchased */
    TryOp( TabRowSetCharString( hRow, 1, "Beans" )  );                     /* Product */
    TryOp( TabRowSetString(     hRow, 2, sUniBeans ) );                    /* uProduct */
    TryOp( TabRowSetDouble(     hRow, 3, 1.08 ) );                         /* Price */
    TryOp( TabRowSetDate(       hRow, 6, 2029, 1, 1 ) );                   /* Expiration date */
    TryOp( TabRowSetCharString( hRow, 7, "Bohnen" ) )                      /* Produkt */

    /* Insert a few rows */
    for ( i = 0; i < 10; ++i ) {
        TryOp( TabRowSetInteger(  hRow, 4, i * 10) );                      /* Quantity */
        TryOp( TabRowSetBoolean(  hRow, 5, i % 2 ) );                      /* Taxed */
        TryOp( TabTableInsert( hTable, hRow ) );
    }

    TryOp( TabRowClose( hRow ) );
    TryOp( TabTableDefinitionClose( hTableDef ) );
}

int main( int argc, char* argv[] )
{
    TAB_HANDLE hExtract;
    TAB_HANDLE hTableDef;
    TAB_HANDLE hTable;
    int bHasTable;

    CreateTableauString( L"order-c.tde", sOrderTde );
    CreateTableauString( L"Extract", sExtract );

    // Initialize Tableau Extract API
    TryOp( TabExtractAPIInitialize() );

    TryOp( TabExtractCreate( &hExtract, sOrderTde ) );
    TryOp( TabExtractHasTable( hExtract, sExtract, &bHasTable ) );

    if ( !bHasTable ) {
        /* Table does not exist; create it. */
        hTableDef = MakeTableDefinition();
        TryOp( TabExtractAddTable( hExtract, sExtract, hTableDef, &hTable ) );
        TryOp( TabTableDefinitionClose( hTableDef ) );
    }
    else {
        /* Open an existing table to add more rows. */
        TryOp( TabExtractOpenTable( hExtract, sExtract, &hTable ) );
    }

    TryOp( TabTableGetTableDefinition( hTable, &hTableDef ) );
    PrintTableDefinition( hTableDef );
 
    InsertData( hTable );

    /* Clean up */
    TryOp( TabTableDefinitionClose( hTableDef ) );
    TryOp( TabExtractClose( hExtract ) );

    // Clean up Tableau Extract API
    TryOp( TabExtractAPICleanup() );

    return 0;
}
