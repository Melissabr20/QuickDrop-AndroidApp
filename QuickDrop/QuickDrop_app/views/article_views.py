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


"""Article catalogue endpoints, including article images and fournisseur-scoped availability."""

class ArticleListAPIView(APIView):
    permission_classes = (permissions.AllowAny,)
    parser_classes = (MultiPartParser, FormParser)

    def get(self, request):
        articles = Article.objects.all()
        serializer = ArticleSerializer(articles, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

    def post(self, request):
        serializer = ArticleSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)





class ArticleDetailAPIView(APIView):
    permission_classes = (permissions.AllowAny,)

    def get(self, request, pk):
        try:
            article = Article.objects.get(pk=pk)
        except Article.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)

        serializer = ArticleSerializer(article)
        return Response(serializer.data)

    def put(self, request, pk):
        try:
            article = Article.objects.get(pk=pk)
        except Article.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)

        serializer = ArticleSerializer(article, data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def delete(self, request, pk):
        try:
            article = Article.objects.get(pk=pk)
        except Article.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)

        article.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)



def get_image(request, image_id):
    try:
        image_instance =Article.objects.get(id=image_id)
        image_instance= image_instance.image
        image_url = request.build_absolute_uri(image_instance.url)
        return JsonResponse({'image_url': image_url})
    except Article.DoesNotExist:
        return JsonResponse({'error': 'Image not found'}, status=404)



class AvailableArticlesView(APIView):
    permission_classes = (permissions.AllowAny,)
    def get(self, request):
        available_articles = Article.objects.filter(listeproduits__stock_disponible__gt=0).distinct()
        serializer = ArticleSerializer(available_articles, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)



class ArticlesNotSharedWithFournisseurView(APIView):
    permission_classes = (permissions.AllowAny,)
    def get(self, request, fournisseur_id):
        fournisseur = Fournisseur.objects.get( id=fournisseur_id)
        shared_articles = Article.objects.filter(listeproduits__fournisseur=fournisseur)
        not_shared_articles = Article.objects.exclude(id__in=shared_articles.values('id'))
        serializer = ArticleSerializer(not_shared_articles, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


class Articles(APIView):
    permission_classes = (permissions.AllowAny,)
    def get(self, request):
        queryset = Article.objects.all()
        serializer = ArticleSerializer(queryset, many=True)
        return Response(serializer.data)
