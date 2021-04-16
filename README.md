# Merritt Sword Service

This microservice is part of the [Merritt Preservation System](https://github.com/CDLUC3/mrt-doc).

## Purpose

This microservice implements the [OSimple Web-service Offering Repository Deposit](http://swordapp.org/about/) 
for the Merritt Preservation System.

This microservice is used by the [Dryad](https://datadryad.org/) service. 
Additional clients of this service are not expected. 

## Component Diagram
![Flowchart](https://github.com/CDLUC3/mrt-doc/raw/master/diagrams/sword.mmd.svg)

## Dependencies

This code depends on the following Merritt Libraries.
- [Merritt Core Library](https://github.com/CDLUC3/mrt-core2)

## For external audiences
This code is not intended to be run apart from the Merritt Preservation System.

See [Merritt Docker](https://github.com/CDLUC3/merritt-docker) for a description of how to build a test instnce of Merritt.

## Build instructions
This code is deployed as a war file. The war file is built on a Jenkins server.

## Test instructions

## Internal Links

### Deployment and Operations at CDL

https://github.com/CDLUC3/mrt-doc-private/blob/main/uc3-mrt-sword.md
