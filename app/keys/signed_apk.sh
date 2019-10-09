#!/usr/bin/env bash
root=$PWD

#!/bin/bash

# setting default key path here

# local OPTIND

# jarsigner -verbose -verify -keystore ${keyPath} -certs ${packagePath}

DEFAULT_KEY_PATH=/Users/sjy/Desktop/ThinkerNote2_key0817

DEFAULT_STORE_PASS="thinkernote"

DEFAULT_ALIASES="will"

DEFAULT_KEY_PASS="thinkernote"

DEFAULT_DIGESTALG=SHA1

DEFAULT_SIGALG=MD5withRSA



sigalg=${DEFAULT_SIGALG}

digestalg=${DEFAULT_DIGESTALG}

keyPath=${DEFAULT_KEY_PATH}

storepass=${DEFAULT_STORE_PASS}

keypass=${DEFAULT_KEY_PASS}

aliases=${DEFAULT_ALIASES}

packagePath=/Users/sjy/Desktop/



IS_VERIFY=false



if [ ! -n "$1" ]; then

    echo "unkonw path, please use apk path"

    exit 1

else

    while getopts "p:k:h:" arg #after param has ":" need option

    do

        case $arg in

            p)

                echo "Package path: $OPTARG"

                packagePath=$OPTARG

                ;;

            k)

                echo "Key Path: $OPTARG"

                keyPath=$OPTARG

                ;;

            h)

                echo "use -p [packagePath] -k [keyPath] -h Show help"

                exit 1

                ;;

            ?)  # other param?

                echo "unkonw argument, please use -p [packagePath] -k [keyPath]"

                exit 1

                ;;

        esac

    done

fi



#echo "sigalg: ${sigalg}"

#echo "digestalg: ${digestalg}"

#echo "keyPath: ${keyPath}"

#echo "storepass: ${storepass}"

#echo "aliases: ${aliases}"

#echo "keypass: ${keypass}"

#echo "packagePath: ${packagePath}"

jarsigner -verbose -digestalg ${digestalg} -sigalg ${sigalg} -keystore ${keyPath} -storepass ${storepass} -keypass ${keypass} ${packagePath} ${aliases}