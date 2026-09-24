from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from django.contrib.auth.models import User
from .models import *

# Register your models here.
   

class TodoUserInline(admin.StackedInline):
    model=TodoUser
class TodoUserAdmin(UserAdmin):
    inlines=(TodoUserInline,)
admin.site.unregister(User)
admin.site.register(User,TodoUserAdmin)




admin.site.register(Livreur)
admin.site.register(Client)
admin.site.register(Fournisseur)
admin.site.register(Livraison)
admin.site.register(Article)
admin.site.register(ListeProduits)
admin.site.register(Adresse_livraison)
admin.site.register(LigneLivraison)



