#
# Build and run C/C++ samples.
#
# Requires g++ 4.4 or later.

LIBROOT = /usr/lib64
RELLIBROOT = ../../../lib64
CFLAGS  = -I$(LIBROOT)/../include -I$(RELLIBROOT)/../include
CXXFLAGS = $(CFLAGS) -std=c++0x
LDFLAGS = -Wl,-rpath,$(LIBROOT)/tableausdk:$(RELLIBROOT)/tableausdk

LIBS = -L$(LIBROOT)/tableausdk -L$(RELLIBROOT)/tableausdk -lTableauCommon
EXTRACT_LIBS = $(LIBS) -lTableauExtract
SERVER_LIBS = $(LIBS) -lTableauServer

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
	rm -f DataExtract.log TableauSDK*.log \
        MakeOrder_c MakeOrder_cpp order-c.tde order-cpp.tde \
        PublishOrder_c PublishOrder_cpp

build-MakeOrder: build-MakeOrder_c build-MakeOrder_cpp

run-MakeOrder: run-MakeOrder_c run-MakeOrder_cpp

build-PublishOrder: build-PublishOrder_c build-PublishOrder_cpp

run-PublishOrder: run-PublishOrder_c run-PublishOrder_cpp

build-MakeOrder_c : MakeOrder.c
	$(CC) $(CFLAGS) $(LDFLAGS) MakeOrder.c $(EXTRACT_LIBS) -o MakeOrder_c

run-MakeOrder_c : build-MakeOrder_c
	rm -f order-c.tde
	./MakeOrder_c
	./MakeOrder_c

build-MakeOrder_cpp : MakeOrder.cpp
	$(CXX) $(CXXFLAGS) $(LDFLAGS) MakeOrder.cpp $(EXTRACT_LIBS) -o MakeOrder_cpp

run-MakeOrder_cpp : build-MakeOrder_cpp
	rm -f order-cpp.tde
	./MakeOrder_cpp
	./MakeOrder_cpp

build-PublishOrder_c : PublishOrder.c
	$(CC) $(CFLAGS) $(LDFLAGS) PublishOrder.c $(SERVER_LIBS) -o PublishOrder_c

run-PublishOrder_c : build-PublishOrder_c
	./PublishOrder_c

build-PublishOrder_cpp : PublishOrder.cpp
	$(CXX) $(CXXFLAGS) $(LDFLAGS) PublishOrder.cpp $(SERVER_LIBS) -o PublishOrder_cpp

run-PublishOrder_cpp : build-PublishOrder_cpp
	./PublishOrder_cpp

