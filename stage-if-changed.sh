#!/usr/bin/env bash

set -eou pipefail

dir=$1

# Atom feeds currently get a new timestamp whenever they are regenerated
# causing them to trigger a redeploy when really no data changed
if [[ $(git diff $dir | filterdiff -p 1 -x '_site/atom*.xml') ]]; then
    echo "Modified files in $dir"
    git add $dir
elif [[ $(git ls-files --other --directory --exclude-standard _site/) ]]; then
    echo "Untracked files in $dir"
    git add $dir
else
    echo "No changes in $dir"
fi
