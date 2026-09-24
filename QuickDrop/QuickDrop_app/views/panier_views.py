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


"""Shopping cart (Panier / LignePanier) endpoints and delivery price calculation."""

class LignesPanierAPIView2(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    authentication_classes = (JWTAuthentication,)


    def get(self, request, panier_id):

        try:
            import json

            client = request.user.client

            panier = Livraison.objects.get(
                pk=panier_id
            )

            lignes_panier = LigneLivraison.objects.filter(
                livraison=panier
            )

            articles_a_recuperer = {
                ligne.article.id
                for ligne in lignes_panier
            }

            fournisseurs = {}
            distances = {}

            # =====================================================
            # 1. Trouver les fournisseurs pour chaque article
            # =====================================================

            for ligne_panier in lignes_panier:

                liste_produit_article_view = (
                    ListeProduitArticle.as_view()
                )

                response = liste_produit_article_view(
                    request._request,
                    pk=ligne_panier.article.pk,
                    quantite=ligne_panier.quantity
                )

                response.render()

                for fournisseur in response.data:

                    id_fournisseur = fournisseur['id']

                    if id_fournisseur not in fournisseurs:

                        fournisseurs[id_fournisseur] = {
                            'Articles': set(),
                            'Distance_Fournisseur': {}
                        }

                    fournisseurs[
                        id_fournisseur
                    ]['Articles'].add(
                        ligne_panier.article.id
                    )

                    # =============================================
                    # Distance Client -> Fournisseur
                    # =============================================

                    distance_view = (
                        DistanceClientFournisseur.as_view()
                    )

                    distance_response = distance_view(
                        request._request,
                        pk1=client.pk,
                        pk2=id_fournisseur
                    )

                    print("=================================") 
                    print("CLIENT :", client.pk) 
                    print("FOURNISSEUR :", id_fournisseur) 
                    print("STATUS :", distance_response.status_code) 
                    print("CONTENT :", distance_response.content) 
                    print("=================================") 
                    distance_data = json.loads( 
                        distance_response.content 
                    ) 

                    distance = distance_data.get("distance") 
                    print("DISTANCE :", distance) 
                    if distance is not None: 
                        fournisseurs[id_fournisseur]['Distance_Client'] = float(distance)
            # =====================================================
            # 2. Calculer les distances entre fournisseurs
            # =====================================================

            fournisseurs_ids = list(
                fournisseurs.keys()
            )

            for i in range(
                len(fournisseurs_ids)
            ):

                for j in range(
                    i + 1,
                    len(fournisseurs_ids)
                ):

                    id_fournisseur1 = (
                        fournisseurs_ids[i]
                    )

                    id_fournisseur2 = (
                        fournisseurs_ids[j]
                    )

                    distance_view = (
                        DistanceFournisseurFournisseur.as_view()
                    )

                    distance_response = distance_view(
                        request._request,
                        pk1=id_fournisseur1,
                        pk2=id_fournisseur2
                    )

                    print("=================================")
                    print("DISTANCE CLIENT -> FOURNISSEUR")
                    print("Client :", client.pk)
                    print("Fournisseur :", id_fournisseur)
                    print("Status :", distance_response.status_code)
                    print("Content :", distance_response.content)
                    print("=================================")

                    try:
                        distance_data = json.loads(
                            distance_response.content
                        )
                    except Exception as e:
                        print("ERREUR JSON :", e)
                        distance_data = {}

                    distance = distance_data.get("distance")

                    print("Distance récupérée :", distance)

                    if distance is not None:
                        fournisseurs[id_fournisseur]['Distance_Client'] = distance



                    if distance is not None:

                        distances[
                            (id_fournisseur1, id_fournisseur2)
                        ] = distance

                        distances[
                            (id_fournisseur2, id_fournisseur1)
                        ] = distance

                        fournisseurs[
                            id_fournisseur1
                        ]['Distance_Fournisseur'][
                            id_fournisseur2
                        ] = distance

                        fournisseurs[
                            id_fournisseur2
                        ]['Distance_Fournisseur'][
                            id_fournisseur1
                        ] = distance
           
            # =====================================================
            # 3. TROUVER LA SOLUTION OPTIMALE
            # =====================================================

            solution = trouver_solution_optimale(
                fournisseurs,
                articles_a_recuperer,
                distances
            )
            
            # =====================================================
            # 4. Aucune solution
            # =====================================================

            if solution is None:

                return Response(
                    {
                        "detail": "Impossible de trouver une solution",
                        "articles_requis": list(articles_a_recuperer),
                        "fournisseurs": fournisseurs,
                        "distances": {
                            f"{k[0]}-{k[1]}": v
                            for k, v in distances.items()
                        }
                    },
                    status=400
                )

            # =====================================================
            # 5. Récupérer la solution
            # =====================================================

            meilleure_solution = solution[
                'fournisseurs'
            ]

            articles_par_fournisseur = solution[
                'articles'
            ]

            print(
                "Fournisseurs sélectionnés :",
                meilleure_solution
            )

            print(
                "Distance totale :",
                solution['distance']
            )

            print(
                "Articles par fournisseur :",
                articles_par_fournisseur
            )

            # =====================================================
            # 6. Mapping article -> ligne
            # =====================================================

            lignes_par_article = {}

            for ligne in lignes_panier:

                lignes_par_article[
                    ligne.article_id
                ] = ligne

            livraisons_creees = []

            # =====================================================
            # 7. Créer les livraisons
            # =====================================================

            for index, fournisseur_id in enumerate(
                meilleure_solution
            ):

                articles = articles_par_fournisseur.get(
                    fournisseur_id,
                    set()
                )

                if not articles:
                    continue

                fournisseur_instance = (
                    Fournisseur.objects.get(
                        pk=fournisseur_id
                    )
                )

                # Premier fournisseur :
                # utiliser le panier existant
                if index == 0:

                    livraison = panier

                    livraison.fournisseur = (
                        fournisseur_instance
                    )

                    livraison.client = client

                    livraison.save()

                # Autres fournisseurs :
                # créer une nouvelle livraison
                else:

                    livraison = Livraison.objects.create(
                        client=client,
                        fournisseur=fournisseur_instance,
                        valide=False
                    )

                livraisons_creees.append(
                    livraison
                )

                # =================================================
                # Affecter les lignes
                # =================================================

                for article_id in articles:

                    ligne = lignes_par_article.get(
                        article_id
                    )

                    if ligne is not None:

                        ligne.livraison = livraison
                        ligne.save()

            # =====================================================
            # 8. Valider les livraisons
            # =====================================================

            for livraison in livraisons_creees:

                livraison.valide = True
                livraison.save()

            # =====================================================
            # 9. Retourner les livraisons
            # =====================================================

            livraisons = Livraison.objects.filter(
                client=client,
                valide=True
            )

            serializer = LivraisonSerializer(
                livraisons,
                many=True
            )

            return Response(
                serializer.data
            )

        except Livraison.DoesNotExist:

            return Response(
                {
                    "message": (
                        "La livraison spécifiée "
                        "n'existe pas."
                    )
                },
                status=status.HTTP_404_NOT_FOUND
            )

        except Exception as e:

            return Response(
                {
                    "message": str(e)
                },
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )




class PanierListCreateAPIView(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    authentication_classes = (JWTAuthentication,)

    def get(self, request):
        client = request.user.client
        panier_courant = Livraison.objects.filter(client=client, valide=False).first()
        if panier_courant:
            serializer = LivraisonSerializer(panier_courant)
            return Response(serializer.data)
        else:
            return Response({"message": "Pas de panier trouvé pour l'utilisateur actuel."})

    def post(self, request):
        # Récupérer le client associé à l'utilisateur authentifié
        client = request.user.client

        # Vérifier si un panier non validé existe déjà pour le client
        panier_non_valide = Livraison.objects.filter(client=client, valide=False).first()

        # Si un panier non validé existe déjà, renvoyer un message d'erreur
        if panier_non_valide:
            return JsonResponse({'id': panier_non_valide.id})

         # Ajouter le client aux données de la requête
        request.data['client'] = client.id

        if 'lignes_panier' in request.data:
           del request.data['lignes_panier']

        # Si aucun panier non validé n'existe, créer un nouveau panier non validé
        serializer = LivraisonSerializer(data=request.data)
        if serializer.is_valid():
            # Assigner le client au panier
            serializer.validated_data['client'] = client
            # Sauvegarder le panier
            panier = serializer.save()
            # Renvoyer la réponse avec l'ID du panier créé
            return Response({'id': panier.id}, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
#recuperer, mettre a jour et supprimer un panier specifique.


class PanierDetailAPIView(APIView):
    permission_classes = (permissions.AllowAny,)
    def get_object(self, pk):
        try:
            return Livraison.objects.get(pk=pk)
        except Livraison.DoesNotExist:
            raise Http404

    def get(self, request, pk):
        panier = self.get_object(pk)
        lignes_panier = panier.lignelivraison_set.all()
        serializer = LivraisonSerializer(panier, context={'lignes_panier': lignes_panier})
        return Response(serializer.data)

    def put(self , request,pk ):
        try:
            liv= Livraison.objects.get(pk=pk)
        except Livraison.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)

        serializer =LivraisonSerializer(liv, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    


    def delete(self, request, pk):
        panier = self.get_object(pk)
        panier.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)
        
#gérer la liste des lignes de panier et la création de nouvelles lignes de panier.


class LignePanierListCreateAPIView(APIView):
    permission_classes = (permissions.AllowAny,)
    def get(self, request):
        lignes_panier = LigneLivraison.objects.all()
        serializer = LigneLivraisonSerializer(lignes_panier, many=True)
        return Response(serializer.data)

    def post(self, request):
        serializer = LigneLivraisonSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

#récupérer, mettre à jour et supprimer une ligne de panier spécifique.


class LignePanierDetailAPIView(APIView):
    permission_classes = (permissions.AllowAny,)
    def get_object(self, pk):
        try:
            return LigneLivraison.objects.get(pk=pk)
        except LigneLivraison.DoesNotExist:
            raise Http404

    def get(self, request, pk):
        ligne_panier = self.get_object(pk)
        serializer = LigneLivraisonSerializer(ligne_panier)
        return Response(serializer.data)

    def put(self, request, pk):
        ligne_panier = self.get_object(pk)
        serializer = LigneLivraisonSerializer(ligne_panier, data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def delete(self, request, pk):
        ligne_panier = self.get_object(pk)
        total=ligne_panier.get_total()
        Panier=ligne_panier.livraison
        montant_actuel=Panier.get_total_price()
        nouveau_montant = montant_actuel - total
        ligne_panier.delete()
        Panier.montant = nouveau_montant
    
        Panier.save()
        return Response(status=status.HTTP_204_NO_CONTENT)





class CalculerPrixLivraison(APIView):
    permission_classes = (permissions.AllowAny,)
    def post(self, request, pk):
        try:
            # Récupérer la livraison à partir de l'ID fourni
            livraison = Livraison.objects.get(pk=pk)
            
            # Récupérer le client associé à cette livraison
            client = livraison.client  # Assurez-vous que le modèle Livraison a une relation avec le modèle Client
            
            # Récupérer l'adresse de livraison associée à ce client
            adresse_livraison = Adresse_livraison.objects.get(id_client=client)
            
            # Récupérer le fournisseur à partir du jeton JWT
            fournisseur = livraison.fournisseur
            
            # Calculer le prix de livraison
            prix_livraison = 0
            if fournisseur.city.casefold() == adresse_livraison.city.casefold():
                prix_livraison = 300
            elif fournisseur.wilaya.casefold() == adresse_livraison.wilaya.casefold():
                prix_livraison = 400
            else:
                prix_livraison = 1000
            
            # Mettre à jour le prix de la livraison
            livraison.prix_livraison = prix_livraison
            livraison.save()
            
            return Response({'prix_livraison': prix_livraison}, status=status.HTTP_200_OK)
        
        except Livraison.DoesNotExist:
            return Response({'error': 'Livraison non trouvée'}, status=status.HTTP_404_NOT_FOUND)
        
        except Adresse_livraison.DoesNotExist:
            return Response({'error': 'Adresse de livraison non trouvée pour ce client'}, status=status.HTTP_404_NOT_FOUND)
        
        except Fournisseur.DoesNotExist:
            return Response({'error': 'Fournisseur non trouvé'}, status=status.HTTP_404_NOT_FOUND)
