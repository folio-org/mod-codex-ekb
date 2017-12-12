#!/bin/bash

OPTIND=1

show_help() {
	cat << EOF
Usage: ${0##*/} [-htkiuon]
Adds the RM API configuration to Okapi for use by mod-codex-ekb. All
options, except -h, are required.

    -h              display this help and exit
    -t TENANT       the tenant name to store the RM API config data
    -k KEY          the RM API key
    -i CUSTOMER_ID  the RM API customer ID
    -u RM_API_URL   the RM API URL
    -o OKAPI_URL    the Okapi URL
    -n OKAPI_TOKEN  a valid Okapi token for the URL passed by -o
EOF
}

tenant="";
rm_api_key="";
rm_api_customer_id="";
rm_api_url="";
okapi_url="";
okapt_token="";

while getopts :ht:k:i:u:o:n: opt; do
	case $opt in
		h)
			show_help
			exit 0
			;;
		t)
			tenant=$OPTARG
			;;
		k)
			rm_api_key=$OPTARG
			;;
		i)
			rm_api_customer_id=$OPTARG
			;;
		u)
			rm_api_url=$OPTARG
			;;
		o)
			okapi_url=$OPTARG
			;;
		n)
			okapi_token=$OPTARG
			;;
		:)
			echo "Option -$OPTARG requires an argument." >$2
			show_help >&2
			exit 1
			;;
		\?)
			echo "Invalid option: -$OPTARG" >$2
			show_help >&2
			exit 1
			;;
		*)
			show_help >&2
			exit 1
			;;
	esac
done

if [[ -z "$tenant" || -z "$rm_api_key" || -z "$rm_api_customer_id" ||
      -z "$rm_api_url" || -z "$okapi_url" || -z "$okapi_token" ]]; then
    echo "Missing required option(s)"
    show_help >&2
    exit 1
fi

module="EKB"
config_name="api_access"

curl -X POST \
  $okapi_url'/configurations/entries' \
  -H 'accept: application/json, text/plain' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -H 'x-okapi-tenant: '$tenant \
  -H 'x-okapi-token: '$okapi_token \
  -d '{
            "module": "'$module'",
            "configName": "'$config_name'",
            "code": "kb.ebsco.customerId",
            "description": "EBSCO RM-API Customer ID",
            "enabled": true,
            "value": "'$rm_api_customer_id'"
}'

curl -X POST \
  $okapi_url'/configurations/entries' \
  -H 'accept: application/json, text/plain' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -H 'x-okapi-tenant: '$tenant \
  -H 'x-okapi-token: '$okapi_token \
  -d '{
            "module": "'$module'",
            "configName": "'$config_name'",
            "code": "kb.ebsco.apiKey",
            "description": "EBSCO RM-API API Key",
            "enabled": true,
            "value": "'$rm_api_key'"
}'

curl -X POST \
  $okapi_url'/configurations/entries' \
  -H 'accept: application/json, text/plain' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -H 'x-okapi-tenant: '$tenant \
  -H 'x-okapi-token: '$okapi_token \
  -d '{
            "module": "'$module'",
            "configName": "'$config_name'",
            "code": "kb.ebsco.url",
            "description": "EBSCO RM-API URL",
            "enabled": true,
            "value": "'$rm_api_url'"
}'
