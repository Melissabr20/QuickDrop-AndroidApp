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


"""Supplier (Fournisseur) listing and detail endpoints."""

class FournisseurAPIViewID(APIView):
    permission_classes = (permissions.AllowAny,)

    def get_object(self, pk):
        try:
            return Fournisseur.objects.get(pk=pk)
        except Fournisseur.DoesNotExist:
            return None


    def get(self, request, pk):
        try:
            fournisseur = Fournisseur.objects.get(id=pk)
            serializer = FournisseurSerializer(fournisseur)
            return Response(serializer.data)
        except Fournisseur.DoesNotExist:
            return Response({"error": "Fournisseur non trouvé"}, status=status.HTTP_404_NOT_FOUND)

    def delete(self, request, pk):
        fournisseur = self.get_object(pk)
        if fournisseur is not None:
            user = fournisseur.user  
            if user:
                user.delete()
            fournisseur.delete()
            return Response(status=status.HTTP_204_NO_CONTENT)
        else:
            return Response(status=status.HTTP_404_NOT_FOUND)

    def put(self , request,pk ):
        try:
           livreur= Fournisseur.objects.get(pk=pk)
        except Fournisseur.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)

        serializer =FouSerializer(livreur, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class FournisseurList (APIView):
    permission_classes = (permissions.AllowAny,)
    def get ( self , request ):
        supplier = Fournisseur.objects.all()
        serializer =FournisseurSerializer(supplier , many = True)
        return  Response(serializer.data)
    

    def post ( self , request):
        serializer = FournisseurSerializer(data = request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(data=serializer.data,status=status.HTTP_201_CREATED)
        return  Response(data=serializer.errors,status=status.HTTP_400_BAD_REQUESTE)



class FournisseurAPIView(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    authentication_classes = (JWTAuthentication,)

    def get(self, request):
        livreur = request.user.fournisseur
        queryset = Livraison.objects.filter(fournisseur=livreur, valide=True).all()
        serializer = LivraisonSerializer(queryset, many=True)
        return Response(serializer.data)

