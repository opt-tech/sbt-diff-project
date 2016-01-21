#!/usr/bin/env bash
set -eu

cwd=`dirname "${0}"`
cd ${cwd}

rm -rf .git

git init
git config user.email "you@example.com"
git config user.name "Your Name"

## master
touch a
git add .
git commit -m "initial"

## modify batchCommon
git checkout -b branch-batchCommon
mkdir -p batchCommon
touch batchCommon/b
git add .
git commit -m "batchCommon"

## modify common
git checkout master
git checkout -b branch-common
mkdir -p common
touch common/c
git add .
git commit -m "common"

## modify web
git checkout master
git checkout -b branch-web
mkdir -p web
touch web/d
git add .
git commit -m "web"

## modify scala script at project/
git checkout master
git checkout -b branch-affectAll
mkdir -p project
touch project/e.scala
git add .
git commit -m "affectAll"

## modify batch2, web and root(root should be excluded)
git checkout master
git checkout -b branch-all
mkdir -p src/main/scala
touch src/main/scala/f
mkdir -p batch2
touch batch2/g
mkdir -p web
touch web/h
git add .
git commit -m "all"
