# ![Image](https://www.knime.com/files/knime_logo_github_40x40_4layers.png) KNIME® Tableau Integration

This repository contains the plugins for the KNIME Tableau Integration which contains a set of KNIME nodes for writing the Tableau `tde` and `hyper` formats and sending extracts to the Tableau server.

## Overview

Contained nodes:

* KNIME Tableau Integration - TDE Format
    * Tableau Writer (TDE)
    * Send to Tableau Server (TDE)
* KNIME Tableau Integration - Hyper Format
    * Tableau Writer (Hyper)
    * Send to Tableau Server (Hyper)

### User Guide

Visit the [KNIME Tableau Integration User Guide](https://docs.knime.com/latest/tableau_integration_user_guide/index.html) for installation instructions and usage examples.

![Workflow Screenshot](https://docs.knime.com/latest/tableau_integration_user_guide/img/03_workflow_example.png)

## Content

The repository contains the following plugins:

* _org.knime.ext.tableau_: General code that can be shared between the nodes for the `tde` format and `hyper` format
* _org.knime.ext.tableau.hyper_: Nodes for the `hyper` format
* _org.knime.ext.tableau.tde_: Nodes for the `tde` format
* _org.knime.ext.tablea.hyper.bin.*_: Native libraries for the `hyper` format
* _org.knime.ext.tablea.tde.bin.*_: Native libraries for the `tde` format

## Development Notes

You can find instructions on how to work with our code or develop extensions for
KNIME Analytics Platform in the _knime-sdk-setup_ repository
on [BitBucket](https://bitbucket.org/KNIME/knime-sdk-setup)
or [GitHub](http://github.com/knime/knime-sdk-setup).

## Join the Community!

* [KNIME Forum](https://tech.knime.org/forum)