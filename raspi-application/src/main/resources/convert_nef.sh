#!/bin/bash
if [ -n "$1" ]; then

        if [ -n "$2" ]; then
            dcraw -c "$1" | cjpeg -quality 100 -optimize -progressive > $2
        else
            echo "Usage : convert_nef.sh <SOURCE_FILE> <DESTINATION_FILE>"
            exit 1
        fi

else
    echo "Usage : convert_nef.sh <SOURCE_FILE> <DESTINATION_FILE>"
    exit 1
fi