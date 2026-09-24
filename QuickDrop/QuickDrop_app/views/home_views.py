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


"""Server-rendered admin/home page views."""

class HomeView(ListView):
    template_name = "map.html"
    model = Livreur

    success_url= "/"
    def get(self,request):
        all_livreurs = Livreur.objects.all()
        all_stores =  Fournisseur.objects.all()
        all_Clients =  Adresse_livraison.objects.all()

        eligable_locations = Livreur.objects.filter(place_id__isnull=False)

        locations_livreur=[]

        for a in  eligable_locations:
            data = {
                'lat' : float(a.lat),
                'lng' : float(a.lng),
                'name' : a.id
            }
            locations_livreur.append (data)


        eligable_locations = Adresse_livraison.objects.filter(place_id__isnull=False)

        locations_client=[]

        for a in  eligable_locations:
            data = {
                'lat' : float(a.lat),
                'lng' : float(a.lng),
                'name' : a.id_client
            }
            locations_client.append (data)

        eligable_locations = Fournisseur.objects.filter(place_id__isnull=False)

        locations_store=[]

        for a in  eligable_locations:
            data = {
                'lat' : float(a.lat),
                'lng' : float(a.lng),
                
            }
            locations_store.append (data)

        context= {
            'livreurs'  : all_livreurs,
            'clients': all_Clients,
            'stores': all_stores,
            'locations_livreur': locations_livreur,
            'locations_client': locations_client,
            'locations_store': locations_store
        }
        return render(request,self.template_name,context)



