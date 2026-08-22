#!/usr/bin/env bash

set -e

if [ $# -ne 1 ]; then
  echo "Usage: $0 <service-name>"
  echo "Example: $0 deployment-approval-service"
  exit 1
fi

SERVICE_NAME="$1"
TEMPLATE_NAME="devex-golden-path"
SONAR_PROJECT_KEY="rgoodin_${SERVICE_NAME}"

echo "Initializing service: $SERVICE_NAME"

replace_in_file() {
  local file="$1"

  if [ -f "$file" ]; then
    sed -i.bak \
      -e "s/${TEMPLATE_NAME}/${SERVICE_NAME}/g" \
      "$file"

    rm -f "${file}.bak"
    echo "Updated $file"
  fi
}

replace_in_file "pom.xml"
replace_in_file "src/main/resources/application.properties"
replace_in_file ".github/copilot-instructions.md"

# SonarQube Cloud uses the GitHub organization-prefixed project key.
if [ -f "pom.xml" ]; then
  sed -i.bak \
    -e "s|<sonar.projectKey>${SERVICE_NAME}</sonar.projectKey>|<sonar.projectKey>${SONAR_PROJECT_KEY}</sonar.projectKey>|" \
    pom.xml

  rm -f pom.xml.bak
fi

echo
echo "Service initialization complete."
echo
echo "Service name:      $SERVICE_NAME"
echo "Sonar project key: $SONAR_PROJECT_KEY"
echo
echo "Next steps:"
echo "  1. Review the generated changes with: git diff"
echo "  2. Import this repository into SonarQube Cloud"
echo "  3. Disable SonarQube Automatic Analysis"
echo "  4. Add SONAR_TOKEN to GitHub Actions secrets"
echo "  5. Add ARTIFACTORY_ACCESS_TOKEN to GitHub Actions secrets"
echo "  6. Review README.md and CLAUDE.md for service-specific documentation updates"