from firebase_admin import db

from Server.code.NearbyATMRequestHandler import handleATMRequests
from Server.code.NearbyContainmentRequestHandler import handleContainmentrequests


# from NearbyATMRequestHandler import handleATMRequests
# from NearbyContainmentRequestHandler import handleContainmentrequests

def handleATMClients(curr_request, root, tablepath1, tablepath2, tablepath3):
    if "resolvedContainment" in curr_request and curr_request["resolvedContainment"] == 'false':

        print(curr_request)
        try:
            handleContainmentrequests(root, tablepath2, curr_request["longitude"], curr_request["latitude"],
                                      curr_request["distance"])
            root.child(tablepath3).update({'resolvedContainment': 'success'})
        except:
            root.child(tablepath3).update({'resolvedContainment': 'failure'})

    if "resolvedATM" in curr_request and curr_request["resolvedATM"] == 'false':
        try:
            handleATMRequests(root, tablepath1, curr_request["placeName"], curr_request["distance"],
                              curr_request["distanceUnit"])
            root.child(tablepath3).update({'resolvedATM': 'success'})
        except:
            root.child(tablepath3).update({'resolvedATM': 'failure'})
