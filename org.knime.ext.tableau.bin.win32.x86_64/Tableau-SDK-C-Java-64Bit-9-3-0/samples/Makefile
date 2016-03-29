#
# Build and run C/C++ samples.
#
# Requires VS 2010 or later.
#
# NOTE: Please ensure your compiler's arch maches that of this
# library. If linking fails with undefined references to Tableau
# symbols, such a mismatch is likely the cause.

PKG_ROOT = ..
CFLAGS  = /I$(PKG_ROOT)/include
CXXFLAGS = $(CFLAGS) /EHsc
LDFLAGS = /LIBPATH:$(PKG_ROOT)/lib tableaucommon.lib
EXTRACT_LDFLAGS = $(LDFLAGS) tableauextract.lib
SERVER_LDFLAGS = $(LDFLAGS) tableauserver.lib
PATH = $(PKG_ROOT)/bin;$(PATH)

usage:
	@echo "usage: make [target]"
	@echo
	@echo "Top-level targets:"
	@echo "  usage(default)           Generate this message"
	@echo "  clean                    Clean up all the generated files"
	@echo "  build-MakeOrder          Build MakeOrder C/C++ samples"
	@echo "  run-MakeOrder            Run MakeOrder C/C++ samples twice"
	@echo "  build-PublishOrder       Build PublishOrder C/C++ samples"
	@echo "  run-PublishOrder         Run PublishOrder C/C++ samples"
	@echo
	@echo "NOTE: To run the PublishOrder sample, you will need to modify the source file to"
	@echo "      provide hostname, username, password, siteID; and possibly the extract file"
	@echo "      path, the project name, and the data source name. Also a Tableau Server will"
	@echo "      need to be installed and running on the hostname you specify, and admin"
	@echo "      privilege is required. For details, please check our online documentation."

clean :
	del DataExtract.log TableauSDK*.log \
        MakeOrder_c.exe MakeOrder_cpp.exe MakeOrder.obj order-c.tde order-cpp.tde \
        PublishOrder_c.exe PublishOrder_cpp.exe PublishOrder.obj

build-MakeOrder: build-MakeOrder_c build-MakeOrder_cpp

run-MakeOrder: run-MakeOrder_c run-MakeOrder_cpp

build-PublishOrder: build-PublishOrder_c build-PublishOrder_cpp

run-PublishOrder: run-PublishOrder_c run-PublishOrder_cpp

build-MakeOrder_c : MakeOrder.c
	$(CC) $(CFLAGS) MakeOrder.c /FeMakeOrder_c.exe /link $(EXTRACT_LDFLAGS)

run-MakeOrder_c : build-MakeOrder_c
	del order-c.tde
	.\MakeOrder_c.exe
	.\MakeOrder_c.exe

build-MakeOrder_cpp : MakeOrder.cpp
	$(CXX) $(CXXFLAGS) MakeOrder.cpp /FeMakeOrder_cpp.exe /link $(EXTRACT_LDFLAGS)

run-MakeOrder_cpp : build-MakeOrder_cpp
	del order-cpp.tde
	.\MakeOrder_cpp.exe
	.\MakeOrder_cpp.exe

build-PublishOrder_c : PublishOrder.c
	$(CC) $(CFLAGS) PublishOrder.c /FePublishOrder_c.exe /link $(SERVER_LDFLAGS)

run-PublishOrder_c : build-PublishOrder_c
	.\PublishOrder_c.exe

build-PublishOrder_cpp : PublishOrder.cpp
	$(CXX) $(CXXFLAGS) PublishOrder.cpp /FePublishOrder_cpp.exe /link $(SERVER_LDFLAGS)

run-PublishOrder_cpp : build-PublishOrder_cpp
	.\PublishOrder_cpp.exe

