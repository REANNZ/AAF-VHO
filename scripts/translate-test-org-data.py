#!/usr/bin/env python3

import json

with open("organisations.json", "r") as orgs_file:
  orgs_in = json.load(orgs_file)

orgs_out = []
for org in orgs_in['organizations']:
  with open(f"orgJSON/organization-{org['id']}.json", "r") as org_file:
    org_details = json.load(org_file)['organization']
  new_org = {
    'id': org['id'],
    'name': org['name'],
    'functioning': org['functioning'],
    'archived': org['archived'],
    'link': org['link'],
    'format': org['format'],
    # new MDTool format
    'active': org['functioning'],
    'organizationInfoData': {
      'en': {
        'OrganizationName': org_details['name'],
        'OrganizationDisplayName':   org_details['displayName']
      }
    }
  }
  orgs_out.append(new_org)

print(json.dumps({'organizations': orgs_out}, indent=2))

