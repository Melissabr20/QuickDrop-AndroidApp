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


"""Delivery (Livraison / LigneLivraison) endpoints, including validation."""

class LigneLivraisonList(APIView):
    def get ( self , request ):
        ligne_liv = LigneLivraison.objects.all()
        serializer = LigneLivraisonSerializer(ligne_liv , many = True)
        return  Response(serializer.data)
    #ajouter une commande sur un article a la BDD
    def post ( self , request):
        serializer = LigneLivraisonSerializer(data = request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(data=serializer.data,status=status.HTTP_201_CREATED)
        return  Response(data=serializer.errors,status=status.HTTP_400_BAD_REQUESTE)
    


class LigneLivraisonDetail(APIView):
    #it works
    def get(self,request, pk):
        try:
            ligne_liv = LigneLivraison.objects.get(pk=pk)
        except LigneLivraison.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)
        serialzer = LigneLivraisonSerializer (ligne_liv )  
        return Response(serialzer.data)

    #there is a probleme hereeee   
    def put(self , request,pk ):
        try:
           ligne_liv= LigneLivraison.objects.get(pk=pk)
        except LigneLivraison.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)

        serializer =LigneLivraisonSerializer(ligne_liv, data=request.data,  partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    #it works
    def  delete(self,request, pk):
        try:
            liv = LigneLivraison.objects.get(pk=pk)
        except LigneLivraison.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)

        liv.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)
    



class LivraisonList (APIView):
    permission_classes=[AllowAny,]
    
    def get ( self , request ):
        liv = Livraison.objects.all()
        serializer =LivraisonSerializer(liv , many = True)
        return  Response(serializer.data)
    
    #ajouter une livraison a la BDD
    def post ( self , request):
        serializer = LivraisonSerializer(data = request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(data=serializer.data,status=status.HTTP_201_CREATED)
        return  Response(data=serializer.errors,status=status.HTTP_400_BAD_REQUESTE)




class LivraisonValide (APIView):
    permission_classes = (permissions.IsAuthenticated,)
    authentication_classes = (JWTAuthentication,)

    def get(self, request):
        client = request.user.client
        panier_courant = Livraison.objects.filter(client=client, valide=True).exclude(status="livre")
        serializer =LivraisonSerializer(panier_courant , many = True)
        return  Response(serializer.data)
    


class LivraisonDetail(APIView):

    def get(self,request, pk):
        try:
           liv = Livraison.objects.get(pk=pk)
        except Livraison.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)
        serialzer = LivraisonSerializer (liv )  
        return Response(serialzer.data)
   
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
    
    def  delete(self,request, pk):
        try:
            liv = Livraison.objects.get(pk=pk)
        except Livraison.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)
        liv.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)

 

