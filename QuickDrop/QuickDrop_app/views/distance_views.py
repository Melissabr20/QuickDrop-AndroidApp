from django.views.generic import ListView
from django.views import View
from django.shortcuts import render, redirect
from ..models import *
from ..routing_client import get_distance_duration, get_distance_matrix_km
from django.http import QueryDict

from geopy.distance import geodesic
from datetime import datetime, timedelta
from django.contrib.auth import authenticate ,login, logout, get_user_model
from django.contrib import messages
from django.contrib.auth.forms import UserCreationForm
from ..forms import SignUpForm, UpdateUserForm, ChangePasswordForm
from django import forms
from django.db.models.signals import post_save
from django.dispatch import receiver
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from ..optimisation import trouver_solution_optimale

from ..serializers import *
from django.http import JsonResponse
from rest_framework.authtoken.models import Token
from rest_framework.validators import UniqueValidator
from django.contrib.auth.password_validation import validate_password
from rest_framework.authentication import TokenAuthentication, SessionAuthentication
from rest_framework.permissions import AllowAny
from rest_framework.exceptions import ValidationError
from ..validations import *
import json
from rest_framework import permissions

from rest_framework_simplejwt.authentication import JWTAuthentication 

from rest_framework_simplejwt.tokens import RefreshToken

from rest_framework import generics

from ortools.constraint_solver import routing_enums_pb2
from ortools.constraint_solver import pywrapcp
from itertools import combinations



import requests
import json
import urllib
from ..globals import GLOBAL_EXCLUDE_IDS
from django.http import Http404
from rest_framework.parsers import MultiPartParser, FormParser


"""Distance/duration calculation endpoints between clients, suppliers and delivery drivers (OSRM-backed)."""

class DistanceView(APIView):
    permission_classes = (permissions.AllowAny,)

    def get(self, request, pk, exclude_id,id_livraison):
        try:
            exclude_ids = [int(x) for x in str(exclude_id).split(',') if x.strip() != '']

            livraison = Livraison.objects.get(pk=id_livraison)

            global GLOBAL_EXCLUDE_IDS
            for eid in exclude_ids:
                if eid != 0 and eid not in GLOBAL_EXCLUDE_IDS:
                    GLOBAL_EXCLUDE_IDS.append(eid)

            store = Fournisseur.objects.get(pk=pk)

            # Vérifier si les coordonnées du fournisseur sont valides
            if store.lat is None or store.lng is None:
                return JsonResponse({'error_message': "Les coordonnées du fournisseur sont manquantes."}, status=400)

            # Obtenir tous les livreurs éligibles en excluant ceux de GLOBAL_EXCLUDE_IDS
            poids_totale = livraison.poids_totale
        
            eligible_livreurs = Livreur.objects.filter(
                place_id__isnull=False,
                capacity__gte=poids_totale
            ).exclude(id__in=GLOBAL_EXCLUDE_IDS)

        
            distances = {}

            # Calculer les distances et durées pour chaque livreur
            store_location = (store.lat, store.lng)

            for livreur in eligible_livreurs:
                # Vérifier si les coordonnées du livreur sont valides
                if livreur.lat is None or livreur.lng is None:
                    continue

                livreur_location = (livreur.lat, livreur.lng)
                element = get_distance_duration(store_location[0], store_location[1],
                                                 livreur_location[0], livreur_location[1])

                if element:
                    distance_km = element.get('distance', {}).get('value')
                    duration_sec = element.get('duration', {}).get('value')
                    distances[livreur.id] = {'distance': distance_km, 'duration': duration_sec}

            # Trouver le livreur avec la durée minimale
            if distances:
                livreur_min_distance = min(distances.items(), key=lambda x: x[1]['duration'])
                duration_text = str(timedelta(seconds=livreur_min_distance[1]['duration']))

                # Retourner le livreur avec la durée minimale
                return JsonResponse({'livreur': livreur_min_distance[0],
                                     'distance': livreur_min_distance[1]['distance'],
                                     'duration': duration_text})
            else:
                return JsonResponse({'error_message': "Aucun livreur éligible trouvé."}, status=404)

        except Fournisseur.DoesNotExist:
            return JsonResponse({'error_message': "Le fournisseur spécifié n'existe pas."}, status=404)
        except Livreur.DoesNotExist:
            return JsonResponse({'error_message': "Aucun livreur trouvé."}, status=404)
        except requests.RequestException:
            return JsonResponse({'error_message': "Erreur du service de calcul d'itinéraire."}, status=500)
        except ValueError:
            return JsonResponse({'error_message': "L'ID fourni n'est pas valide. Veuillez fournir un entier."}, status=400)



class ViderListe(APIView):
    permission_classes = (permissions.AllowAny,)
    def post(self, request):
        global GLOBAL_EXCLUDE_IDS
        GLOBAL_EXCLUDE_IDS.clear()
        return JsonResponse({'message': 'La liste des IDs exclus a été réinitialisée.'}, status=200)




class DistanceClientSupplier(APIView):
    permission_classes = (permissions.AllowAny,)
    def get(self, request, client_id, supplier_id):
        try:
            client = Adresse_livraison.objects.get(id_client=client_id)
            supplier = Fournisseur.objects.get(pk=supplier_id)

            element = get_distance_duration(client.lat, client.lng, supplier.lat, supplier.lng)
            if not element:
                return JsonResponse({'error_message': "Impossible de calculer la distance."}, status=500)

            return JsonResponse({
                'distance': element['distance']['value'] / 1000,
                'duration': element['duration']['value']
            })

        except Client.DoesNotExist:
            return JsonResponse({'error_message': "The specified client does not exist."}, status=404)
        except Fournisseur.DoesNotExist:
            return JsonResponse({'error_message': "The specified supplier does not exist."}, status=404)
        



class DistanceSupplierDelivrer(APIView):
    permission_classes = (permissions.AllowAny,)
    def get(self, request, supplier_id, delivrer_id):
        try:
            supplier = Adresse_livraison.objects.get(pk=supplier_id)
            delivrer = Livreur.objects.get(pk=delivrer_id)

            element = get_distance_duration(supplier.lat, supplier.lng, delivrer.lat, delivrer.lng)
            if not element:
                return JsonResponse({'error_message': "Impossible de calculer la distance."}, status=500)

            return JsonResponse({
                'distance': element['distance']['value'] / 1000,
                'duration': element['duration']['value']
            })
        
        except Livreur.DoesNotExist:
            return JsonResponse({'error_message': "The specified client does not exist."}, status=404)
        except Fournisseur.DoesNotExist:
            return JsonResponse({'error_message': "The specified supplier does not exist."}, status=404)
        




class DistanceClientFournisseur(APIView):
    permission_classes = (permissions.AllowAny,)
    def get(self, request, pk1, pk2):
        try:
            client = Client.objects.get(pk=pk1)
            fournisseur = Fournisseur.objects.get(pk=pk2)

            adresse_livraison = Adresse_livraison.objects.get(id_client=4)
            print(adresse_livraison.get_full_address())
          

            fournisseur_location = (fournisseur.lat, fournisseur.lng)

           
            element = get_distance_duration(adresse_livraison.lat, adresse_livraison.lng,
                                             fournisseur_location[0], fournisseur_location[1])
            
            if element:
                distance_km = element.get('distance', {}).get('value') / 1000
                duration_sec = element.get('duration', {}).get('value')
                duration_text = str(timedelta(seconds=duration_sec))

                return JsonResponse({
                    'distance': distance_km,
                    'duration': duration_text
                })

            return JsonResponse({'error_message': "Impossible de calculer la distance."}, status=500)
        except Client.DoesNotExist:
            return JsonResponse({'error_message': "Le client spécifié n'existe pas."}, status=404)
        except Adresse_livraison.DoesNotExist:
            return JsonResponse({'error_message': "Aucune adresse de livraison trouvée pour ce client."}, status=404)
        except Fournisseur.DoesNotExist:
            return JsonResponse({'error_message': "Le fournisseur spécifié n'existe pas."}, status=404)
        except requests.RequestException:
            return JsonResponse({'error_message': "Erreur du service de calcul d'itinéraire."}, status=500)



class DistanceFournisseurFournisseur(APIView):
    permission_classes = (permissions.AllowAny,)
    def get(self, request, pk1, pk2):
        try:
            fournisseur1 = Fournisseur.objects.get(pk=pk1)
            fournisseur = Fournisseur.objects.get(pk=pk2)

            fournisseur1_location = (fournisseur1.lat, fournisseur1.lng)

            fournisseur_location = (fournisseur.lat, fournisseur.lng)

            element = get_distance_duration(fournisseur1.lat, fournisseur1.lng,
                                             fournisseur_location[0], fournisseur_location[1])
            if element:
                distance_km = element.get('distance', {}).get('value') / 1000
                duration_sec = element.get('duration', {}).get('value')
                duration_text = str(timedelta(seconds=duration_sec))

                return JsonResponse({
                    'distance': distance_km,
                    'duration': duration_text
                })

            return JsonResponse({'error_message': "Impossible de calculer la distance."}, status=500)

        except Client.DoesNotExist:
            return JsonResponse({'error_message': "Le client spécifié n'existe pas."}, status=404)
        except Adresse_livraison.DoesNotExist:
            return JsonResponse({'error_message': "Aucune adresse de livraison trouvée pour ce client."}, status=404)
        except Fournisseur.DoesNotExist:
            return JsonResponse({'error_message': "Le fournisseur spécifié n'existe pas."}, status=404)
        except requests.RequestException:
            return JsonResponse({'error_message': "Erreur du service de calcul d'itinéraire."}, status=500)




class DistanceMinFournisseurClient(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    authentication_classes = (JWTAuthentication,) 
    
    def get(self, request,fournisseurs_ids):
        try:
            client = request.user.client

            fournisseurs_ids = fournisseurs_ids.split(",")

            # Convertissez chaque sous-chaîne en entier
            fournisseurs_ids = [int(id) for id in fournisseurs_ids]

            adresse_livraison = Adresse_livraison.objects.get(id_client=client.id)
            eligible_fournisseurs = Fournisseur.objects.filter(id__in=fournisseurs_ids)
            if not eligible_fournisseurs:
                return JsonResponse({'error_message': "Aucun fournisseur éligible trouvé."}, status=404)


            distances = {}

            for fournisseur in eligible_fournisseurs:
                fournisseur_location = (fournisseur.lat, fournisseur.lng)
                element = get_distance_duration(adresse_livraison.lat, adresse_livraison.lng,
                                                 fournisseur_location[0], fournisseur_location[1])
                if element:
                    distance_km = element.get('distance', {}).get('value')
                    duration_sec = element.get('duration', {}).get('value')
                    if distance_km is not None and duration_sec is not None:
                        distances[fournisseur.id] = {'distance': distance_km, 'duration': duration_sec}

            if not distances:
                return JsonResponse({'error_message': "Aucune distance valide trouvée pour les fournisseurs."}, status=404)

            fournisseur_min_distance = min(distances.items(), key=lambda x: x[1]['duration'])
            duration_text = str(timedelta(seconds=fournisseur_min_distance[1]['duration']))

            return JsonResponse({'fournisseur': fournisseur_min_distance[0],
                                 'distance': fournisseur_min_distance[1]['distance'],
                                 'duration': duration_text})

        except Client.DoesNotExist:
            return JsonResponse({'error_message': "Le client spécifié n'existe pas."}, status=404)
        except Adresse_livraison.DoesNotExist:
            return JsonResponse({'error_message': "Aucune adresse de livraison trouvée pour ce client."}, status=404)
        except requests.RequestException:
            return JsonResponse({'error_message': "Erreur du service de calcul d'itinéraire."}, status=500)



class DistanceLivreurClient(APIView):
    def get(self, request, pk1, pk2):
        try:
            client = Client.objects.get(pk=pk1)

            adresse_livraison = Adresse_livraison.objects.get(id_client=pk1)
            livreur = Livreur.objects.get(pk=pk2)

            livreur_location = (livreur.lat, livreur.lng)
            element = get_distance_duration(adresse_livraison.lat, adresse_livraison.lng,
                                             livreur_location[0], livreur_location[1])

            # Extraire la distance et la durée à partir du résultat
            if element:
                distance_km = element.get('distance', {}).get('text')
                duration_min = element.get('duration', {}).get('text')
            else:
                distance_km = None
                duration_min = None

            # Retourner la distance et la durée dans la réponse
            return Response({'Client':client.id   ,'lilvreur':livreur.id   ,'distance_km': distance_km, 'duration_min': duration_min})

        except Client.DoesNotExist:
            return Response({'error_message': "Le client spécifié n'existe pas."}, status=404)
        except Adresse_livraison.DoesNotExist:
            return Response({'error_message': "Aucune adresse de livraison trouvée pour ce client."}, status=404)
        except Livreur.DoesNotExist:
            return Response({'error_message': "Le livreur spécifié n'existe pas."}, status=404)
        except requests.RequestException:
            return Response({'error_message': "Erreur du service de calcul d'itinéraire."}, status=500)



