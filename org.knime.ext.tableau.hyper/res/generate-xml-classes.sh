#!/bin/sh
xjc \
	-d ../src/ \
	-p org.knime.ext.tableau.hyper.sendtable.api.binding \
	./ts-api_2_8.xsd
