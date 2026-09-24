from django.apps import apps
from rest_framework import serializers

from django.contrib.auth.models import User,Group
from .models import *
from django.contrib.auth import  get_user_model, authenticate
from django.contrib.auth import update_session_auth_hash

from rest_framework_simplejwt.tokens import RefreshToken

UserModel = get_user_model()
class GroupSerializer(serializers.HyperlinkedModelSerializer):
    class Meta:
        model = Group
        fields = ['url', 'name']

def get_tokens_for_user(user):
    refresh = RefreshToken.for_user(user)

    return {
        'refresh': str(refresh),
        'access': str(refresh.access_token),
    }

    
class UserRegisterSerializer(serializers.ModelSerializer):
    phone_number = serializers.CharField(write_only=True)
    
    class Meta:
        model = UserModel
        fields = ['id', 'username', 'email', 'password', 'phone_number']

    def create(self, validated_data):
        phone_number = validated_data.pop('phone_number', '09890')
        user_obj = UserModel.objects.create_user(
            email=validated_data['email'],
            username=validated_data['username'],
            password=validated_data['password'],
        )
        TodoUser.objects.create(user=user_obj, phone_number=phone_number)
        user_obj.phone_number = phone_number
        user_obj.username = validated_data['username']
        user_obj.set_password(validated_data['password'])
        user_obj.save()

        token = get_tokens_for_user(user_obj)
        access = token['access']

        return user_obj, access    
    
class UpdateUserSerializer(serializers.ModelSerializer):
    phone_number = serializers.CharField(source='todouser.phone_number')  # Utilisez 'todouser.phone_number' comme source

    class Meta:
        model = UserModel
        fields = ['username', 'first_name', 'last_name', 'email', 'phone_number']

    def update(self, instance, validated_data):
        instance.username = validated_data.get('username', instance.username)
        instance.first_name = validated_data.get('first_name', instance.first_name)
        instance.last_name = validated_data.get('last_name', instance.last_name)
        instance.email = validated_data.get('email', instance.email)
        todouser_data = validated_data.pop('todouser', '09890')
        if todouser_data:
            phone_number = todouser_data.get('phone_number')
            instance.todouser.phone_number = phone_number 
            instance.todouser.save() 
        instance.save()
        return instance



class UpdatePasswordSerializer(serializers.Serializer):
    old_password = serializers.CharField(required=True)
    new_password = serializers.CharField(required=True)
    confirm_new_password = serializers.CharField(required=True)

    def validate_old_password(self, value):
        user = self.context['request'].user
        if not user.check_password(value):
            raise serializers.ValidationError("L'ancien mot de passe est incorrect.")
        return value

    def validate(self, data):
        if data['new_password'] != data['confirm_new_password']:
            raise serializers.ValidationError("Les nouveaux mots de passe ne correspondent pas.")
        return data

    def save(self):
        user = self.context['request'].user
        user.set_password(self.validated_data['new_password'])
        user.save()
        update_session_auth_hash(self.context['request'], user)





        

class TodoUserSerializer(serializers.ModelSerializer):
    class Meta:
        model = TodoUser
        fields = [ 'phone_number']

class UserSerializer(serializers.ModelSerializer):
    phone=TodoUserSerializer(source='todouser')
    class Meta:
        model = UserModel
        fields = ['id', 'username','first_name','last_name', 'email','phone']

   


class ClientSerializer(serializers.ModelSerializer):
    user = UserSerializer()

    class Meta:
        model = Client
        fields = [ 'id','user']

class LivreurSerializer(serializers.ModelSerializer):
    user = UserSerializer(required=False)

    class Meta:
        model = Livreur
        fields = ['id','city', 'wilaya', 'adress', 'lat', 'lng', 'place_id', 'user','actif']
        extra_kwargs = {
            'user': {'required': False},
        }

class FournisseurSerializer(serializers.ModelSerializer):
    user = UserSerializer()

    class Meta:
        model = Fournisseur
        fields = '__all__'


class ArticleSerializer(serializers.ModelSerializer):
    class Meta:
            model = Article
            fields = '__all__'
        
class Adresse_livraisonSerializer(serializers.ModelSerializer):
    class Meta:
            model = Adresse_livraison
            fields = '__all__'
class LigneLivraisonSerializer(serializers.ModelSerializer):
    class Meta:
        model = LigneLivraison
        fields = ['id','article','quantity','livraison']
class LivraisonSerializer(serializers.ModelSerializer):
    lignes_livraison = LigneLivraisonSerializer(many=True, read_only=True)
    
    class Meta:
            model = Livraison
            fields =['id', 'client', 'lignes_livraison','livreur','fournisseur','date','montant','status','valide']
            extra_kwargs = {
                'client': {'required': False},
            }  
    def to_representation(self, instance):
        representation = super().to_representation(instance)
        lignes_livraison= LigneLivraison.objects.filter(livraison=instance)
        representation['lignes_livraison'] = LigneLivraisonSerializer(lignes_livraison, many=True).data
        return representation


class ListeProduitsSerializer(serializers.ModelSerializer):
    class Meta:
            model = ListeProduits
            fields = '__all__'
