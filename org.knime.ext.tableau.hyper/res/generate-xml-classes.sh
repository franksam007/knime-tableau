#!/bin/sh
xjc \
	-d ../src/ \
	-p org.knime.ext.tableau.hyper.sendtable.api.binding \
	./ts-api_3_1.xsd
