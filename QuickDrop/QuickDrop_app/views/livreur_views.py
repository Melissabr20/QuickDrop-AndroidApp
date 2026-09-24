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


"""Delivery driver (Livreur) endpoints: profile, address, route optimisation, assigned deliveries."""

class LivreurAddress(APIView):
    permission_classes = (permissions.AllowAny,)

    def get_object(self, livreur_id):
        try:
            return Livreur.objects.get(id=livreur_id)
        except Livreur.DoesNotExist:
            raise Http404

    def get(self, request, livreur_id, format=None):
        livreur = self.get_object(livreur_id)
        serializer = LivreurSerializer(livreur)
        return Response(serializer.data)

    def put(self, request, livreur_id, format=None):
        livreur = self.get_object(livreur_id)
        livreur.modify_livreur(
            address=request.data.get('address'),
            city=request.data.get('city'),
            wilaya=request.data.get('wilaya')
        )
        livreur.calculate_address()

        serializer = LivreurSerializer(livreur, data=request.data)  # Utilisez request.data pour les données
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)




class LivreurList (APIView):
    permission_classes = (permissions.AllowAny,)
    def get ( self , request ):
        livreur = Livreur.objects.all()
        serializer =LivreurSerializer(livreur , many = True)
        return  Response(serializer.data)
    

    def post ( self , request):
        serializer = LivreurSerializer(data = request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(data=serializer.data,status=status.HTTP_201_CREATED)
        return  Response(data=serializer.errors,status=status.HTTP_400_BAD_REQUESTE)



class UpdateLivreurDetail(APIView):
    permission_classes=(permissions.AllowAny,)

    def get_object(self, pk):
        try:
            return Livreur.objects.get(pk=pk)
        except Livreur.DoesNotExist:
            return None

    def get(self, request, pk):
        livreur = self.get_object(pk)
        if livreur is not None:
            serializer = LivreurSerializer(livreur)
            return Response(serializer.data)
        else:
            return Response(status=status.HTTP_404_NOT_FOUND)

    def put(self , request,pk ):
        try:
           livreur= Livreur.objects.get(pk=pk)
        except Livreur.DoesNotExist:
            return Response(status=status.HTTP_404_NOT_FOUND)

        serializer =LivreurSerializer(livreur, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def delete(self, request, pk):
        livreur = self.get_object(pk)
        if livreur is not None:
            user = livreur.user  # Supposons que le champ qui relie le livreur à l'utilisateur s'appelle 'user'
            if user:
                user.delete()
            livreur.delete()
            return Response(status=status.HTTP_204_NO_CONTENT)
        else:
            return Response(status=status.HTTP_404_NOT_FOUND)
    



class GetLivreurRoad(APIView):
    permission_classes = (permissions.AllowAny,)

    def create_distance_matrix(self, data):
        addresses = [address['cord'] for address in data["addresses"]]

        # Afficher les coordonnées
        print(addresses)

        # OSRM's table service returns the full NxN matrix in a single request
        # (no need for Google's max_elements=100 chunking).
        return get_distance_matrix_km(addresses)
    
    def create_data_model(self, data2,capacities,demands):
        data = {}
        data["distance_matrix"] = data2
        data["pickups_deliveries"] = []
        data["demands"] =demands
        data["vehicle_capacities"] =capacities

        for i in range(1, len(data)-1, 2):
            data['pickups_deliveries'].append([i, i+1])    

        print(data['pickups_deliveries'])   
            
        data["num_vehicles"] = 1
        data["depot"] = 0
        return data
    
    def print_solution(self, data, manager, routing, solution,data2):
        """Returns solution as a dictionary."""
        result = {'routes': [], 'total_distance': 0}
        for vehicle_id in range(data["num_vehicles"]):
            index = routing.Start(vehicle_id)
            route = {'nodes': []}
            route_distance = 0
            while not routing.IsEnd(index):
                route['nodes'].append(data2[manager.IndexToNode(index)])
                previous_index = index
                index = solution.Value(routing.NextVar(index))
                route_distance = route_distance + routing.GetArcCostForVehicle(
                    previous_index, index, vehicle_id
                )
                

            route['nodes'].append(data2[manager.IndexToNode(index)])
            route['distance'] = route_distance
        
            result['routes'].append(route)
            result['total_distance'] += route_distance
        return result['routes']

    def get(self, request, pk):
        livraisons = Livraison.objects.filter(livreur=pk,status='en_attente',valide=True)
        if not livraisons:
            return JsonResponse({'error': 'No Livraison found for this livreur.'}, status=404)

        livreur = livraisons[0].livreur

        print("Livreur:", livreur.id)
        print("LAT:", livreur.lat)
        print("LNG:", livreur.lng)

        data2 = {'addresses': [{'type': 'livreur','id_ord':livraisons[0].id, 'capacity':livraisons[0].livreur.capacity ,'id': pk, 'cord':f"{livraisons[0].livreur.lat},{livraisons[0].livreur.lng}"}]}
        delivrer_capacity =[]
        demandes =[0]
        delivrer_capacity.append(data2['addresses'][0]['capacity'])
        print(delivrer_capacity)

        for liv in livraisons:  
            # Obtenir l'adresse de livraison du client
            addressliv = Adresse_livraison.objects.get(id_client=liv.client.id)

             # Ajouter les coordonnées du fournisseur à la liste
            data2['addresses'].append({'type': 'fournisseur','id_ord': liv.id,'capacity':liv.poids_totale ,'id': liv.fournisseur.id, 'cord': f"{liv.fournisseur.lat},{liv.fournisseur.lng}"})
            demandes.append(liv.poids_totale)
            
            # Ajouter les coordonnées de l'adresse de livraison à la liste
            data2['addresses'].append({'type': 'client','id_ord': liv.id,'capacity':(-1)*liv.poids_totale ,'id': liv.client.id, 'cord': f"{addressliv.lat},{addressliv.lng}"})
            demandes.append((-1)*liv.poids_totale)
            
           

        print(demandes)

        distance_matrix = self.create_distance_matrix(data2)
        data = self.create_data_model(distance_matrix,delivrer_capacity,demandes)

        # Create the routing index manager.
        manager = pywrapcp.RoutingIndexManager(
        len(data["distance_matrix"]), data["num_vehicles"], data["depot"])

        # Create Routing Model.
        routing = pywrapcp.RoutingModel(manager)

        # Create and register a transit callback.
        def distance_callback(from_index, to_index):
            """Returns the distance between the two nodes."""
            # Convert from routing variable Index to distance matrix NodeIndex.
            from_node = manager.IndexToNode(from_index)
            to_node = manager.IndexToNode(to_index)
            return data["distance_matrix"][from_node][to_node]
        
        

        transit_callback_index = routing.RegisterTransitCallback(distance_callback)

        # Define cost of each arc.
        routing.SetArcCostEvaluatorOfAllVehicles(transit_callback_index)

        # Add Capacity constraint.
        def demand_callback(from_index):
            """Returns the demand of the node."""
            # Convert from routing variable Index to demands NodeIndex.
            from_node = manager.IndexToNode(from_index)
            return data["demands"][from_node]
        

        demand_callback_index = routing.RegisterUnaryTransitCallback(demand_callback)
        routing.AddDimensionWithVehicleCapacity(
            demand_callback_index,
            0,  # null capacity slack
            data["vehicle_capacities"],  # vehicle maximum capacities
            True,  # start cumul to zero
            "Capacity",
        )
       


        distance_dimension = routing.GetDimensionOrDie("Capacity")
        distance_dimension.SetGlobalSpanCostCoefficient(100)

        # Define pick-up and delivery constraints.
        for pickup, delivery in data["pickups_deliveries"]:
            routing.AddPickupAndDelivery(manager.NodeToIndex(pickup), manager.NodeToIndex(delivery))
            routing.solver().Add(routing.VehicleVar(manager.NodeToIndex(pickup)) == routing.VehicleVar(manager.NodeToIndex(delivery)))
            routing.solver().Add(distance_dimension.CumulVar(manager.NodeToIndex(pickup)) <= distance_dimension.CumulVar(manager.NodeToIndex(delivery)))
        
        print("hiiiiiiiiii")

        # Setting first solution heuristic.
        search_parameters = pywrapcp.DefaultRoutingSearchParameters()
        search_parameters.first_solution_strategy = (
            routing_enums_pb2.FirstSolutionStrategy.PATH_CHEAPEST_ARC
        )

        search_parameters.local_search_metaheuristic = (
            routing_enums_pb2.LocalSearchMetaheuristic.GUIDED_LOCAL_SEARCH
        )

        search_parameters.time_limit.FromSeconds(1)

        # Solve the  problem.
        solution = routing.SolveWithParameters(search_parameters)

        """manager = pywrapcp.RoutingIndexManager(
            len(data["distance_matrix"]), data["num_vehicles"], data["depot"]
        )

        # Create Routing Model.
        routing = pywrapcp.RoutingModel(manager)

        # Define cost of each arc.
        def distance_callback(from_index, to_index):
            #Returns the manhattan distance between the two nodes.
            # Convert from routing variable Index to distance data NodeIndex.
            from_node = manager.IndexToNode(from_index)
            to_node = manager.IndexToNode(to_index)
            return data["distance_matrix"][from_node][to_node]

        transit_callback_index = routing.RegisterTransitCallback(distance_callback)
        routing.SetArcCostEvaluatorOfAllVehicles(transit_callback_index)

        # Add Distance constraint.
        dimension_name = "Distance"
        routing.AddDimension(
            transit_callback_index,
            0,  # no slack
            70000,  # vehicle maximum travel distance
            True,  # start cumul to zero
            dimension_name,
        )
        distance_dimension = routing.GetDimensionOrDie(dimension_name)
        distance_dimension.SetGlobalSpanCostCoefficient(100)

        # Define Transportation Requests.
        for request in data["pickups_deliveries"]:
            pickup_index = manager.NodeToIndex(request[0])
            delivery_index = manager.NodeToIndex(request[1])
            routing.AddPickupAndDelivery(pickup_index, delivery_index)
            routing.solver().Add(
                routing.VehicleVar(pickup_index) == routing.VehicleVar(delivery_index)
            )
            routing.solver().Add(
                distance_dimension.CumulVar(pickup_index)
                <= distance_dimension.CumulVar(delivery_index)
            )

        # Setting first solution heuristic.
        search_parameters = pywrapcp.DefaultRoutingSearchParameters()
        search_parameters.first_solution_strategy = (
            routing_enums_pb2.FirstSolutionStrategy.PARALLEL_CHEAPEST_INSERTION
        )

        # Solve the problem.
        solution = routing.SolveWithParameters(search_parameters)
        
        result = self.print_solution(data,manager,routing,solution,data2["addresses"])"""

        result = self.print_solution(data,manager,routing,solution,data2["addresses"])
        if solution: 
            return JsonResponse( {'success': result[0].get("nodes")}, status=200)
        else:
            return JsonResponse({'error': 'No Solution Found'}, status=500)





class PanierListByLivreurAPIView(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    authentication_classes = (JWTAuthentication,)

    def get(self, request):
        livreur = request.user.livreur
        queryset = Livraison.objects.filter(livreur=livreur, valide=True).all()
        serializer = LivraisonSerializer(queryset, many=True)
        return Response(serializer.data)
    
    


class PanierListByLivreurHisAPIView(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    authentication_classes = (JWTAuthentication,)

    def get(self, request):
        livreur = request.user.livreur
        queryset = Livraison.objects.filter(livreur=livreur, valide=True,status='Livre').all()
        serializer = LivraisonSerializer(queryset, many=True)
        return Response(serializer.data)
    
