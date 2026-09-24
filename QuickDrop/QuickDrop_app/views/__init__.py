"""
Views package for QuickDrop_app.

The views used to live in a single monolithic views.py file. They are now split
into focused modules below, grouped by domain. Every name is re-exported here so
existing imports such as `from . import views` / `from .views import *` keep working
unchanged.
"""

from .home_views import *  # noqa: F401,F403
from .auth_views import *  # noqa: F401,F403
from .client_views import *  # noqa: F401,F403
from .article_views import *  # noqa: F401,F403
from .produit_views import *  # noqa: F401,F403
from .panier_views import *  # noqa: F401,F403
from .adresse_views import *  # noqa: F401,F403
from .livraison_views import *  # noqa: F401,F403
from .fournisseur_views import *  # noqa: F401,F403
from .livreur_views import *  # noqa: F401,F403
from .distance_views import *  # noqa: F401,F403
from .admin_views import *  # noqa: F401,F403
