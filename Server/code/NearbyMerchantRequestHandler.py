import requests
import json


def getMerchantLocations(merchantCategoryCode, range, unit, lat, lon):
    url = 'https://sandbox.api.visa.com/merchantlocator/v1/locator'

    headers = {'Accept': 'application/json'}

    json_file = open('../Resources/merchant_locator_payload.json', )

    payload = json.load(json_file)

    user_id = 'XC2ZRLC7JCX0B2PR5SWK21t2Or5cIWdZsdfe9DAw4vfq30040'

    password = 'BdjGC5WegJZ13qit60o2'

    key = '../Resources/merchant_locator_api_key.pem'

    cert = '../Resources/merchant_locator_api_cert.pem'

    payload["searchAttrList"]["merchantCategoryCode"] = merchantCategoryCode
    payload["searchAttrList"]["distance"] = range
    payload["searchAttrList"]["latitude"] = lat
    payload["searchAttrList"]["longitude"] = lon
    payload["searchAttrList"]["distanceUnit"] = unit

    resp = requests.post(url=url, cert=(cert, key), auth=(user_id, password), headers=headers, json=payload, timeout=10)

    data = json.loads(resp.content)

    print(data)
    return data


def handleMerchantRequests(root, tablepath, merchantCategoryCode, range, unit, lat, lon):
    response = getMerchantLocations(merchantCategoryCode, range, unit, lat, lon)

    if 'response' in response["merchantLocatorServiceResponse"]:
        for res in response["merchantLocatorServiceResponse"]["response"]:
            print("Visa Store Name : ", res["responseValues"]["visaStoreName"])
            print("Distance : ", res["responseValues"]["distance"])
            print("Latitude : ", res["responseValues"]["locationAddressLatitude"])
            print("Longitude : ", res["responseValues"]["locationAddressLongitude"])
            print("Category : ", res["responseValues"]["merchantCategoryCodeDesc"])
            print("StoreId : ", res['responseValues']['visaStoreId'])

            root.child(tablepath).push({
                "visaStoreName": res["responseValues"]["visaStoreName"],
                "distance": res["responseValues"]["distance"],
                "latitude": res["responseValues"]["locationAddressLatitude"],
                "longitude": res["responseValues"]["locationAddressLongitude"],
                "category": res["responseValues"]["merchantCategoryCodeDesc"],
                "StoreId": res['responseValues']['visaStoreId']
            })
# getMerchantLocations(['5814'], 2, "M", "37.363922", "-121.929163")
