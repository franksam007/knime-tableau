#
# Build and run C/C++ samples.
#

# Change FRAMEWORK_ROOT to wherever you install the framework
FRAMEWORK_ROOT = ../Frameworks

CFLAGS  = -g -F$(FRAMEWORK_ROOT)
CXXFLAGS = $(CFLAGS) -std=c++11 -stdlib=libc++

LDFLAGS = -framework TableauCommon
EXTRACT_LDFLAGS = $(LDFLAGS) -framework TableauExtract
SERVER_LDFLAGS = $(LDFLAGS) -framework TableauServer

export DYLD_FALLBACK_FRAMEWORK_PATH := $(FRAMEWORK_ROOT):$(DYLD_FALLBACK_FRAMEWORK_PATH)

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
	rm -fr DataExtract.log TableauSDK*.log \
        MakeOrder_c MakeOrder_c.dSYM order-c.tde \
        MakeOrder_cpp MakeOrder_cpp.dSYM order-cpp.tde \
        PublishOrder_c PublishOrder_c.dSYM \
        PublishOrder_cpp PublishOrder_cpp.dSYM

build-MakeOrder: build-MakeOrder_c build-MakeOrder_cpp

run-MakeOrder: run-MakeOrder_c run-MakeOrder_cpp

build-PublishOrder: build-PublishOrder_c build-PublishOrder_cpp

run-PublishOrder: run-PublishOrder_c run-PublishOrder_cpp

build-MakeOrder_c : MakeOrder.c
	$(CC) $(CFLAGS) $(EXTRACT_LDFLAGS) MakeOrder.c -o MakeOrder_c

run-MakeOrder_c : build-MakeOrder_c
	rm -f order-c.tde
	./MakeOrder_c
	./MakeOrder_c

build-MakeOrder_cpp : MakeOrder.cpp
	$(CXX) $(CXXFLAGS) $(EXTRACT_LDFLAGS) MakeOrder.cpp -o MakeOrder_cpp

run-MakeOrder_cpp : build-MakeOrder_cpp
	rm -f order-cpp.tde
	./MakeOrder_cpp
	./MakeOrder_cpp

build-PublishOrder_c : PublishOrder.c
	$(CC) $(CFLAGS) $(SERVER_LDFLAGS) PublishOrder.c -o PublishOrder_c

run-PublishOrder_c : build-PublishOrder_c
	./PublishOrder_c

build-PublishOrder_cpp : PublishOrder.cpp
	$(CXX) $(CXXFLAGS) $(SERVER_LDFLAGS) PublishOrder.cpp -o PublishOrder_cpp

run-PublishOrder_cpp : build-PublishOrder_cpp
	./PublishOrder_cpp

