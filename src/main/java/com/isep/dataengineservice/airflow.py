from airflow.models import DAG
from datetime import datetime, timedelta
from airflow.providers.http.operators.http import SimpleHttpOperator

default_args ={
    'owner': 'omayos',
    'email': 'yousseftalibi@outlook.com',
    'retries': 2,
    'retry_delay': timedelta(minutes=15),
    'start_date': datetime(2023, 6, 10),
}

with DAG('placesProject', default_args=default_args) as dag:
    city = "rome"

    storeGeoPositionsTask = SimpleHttpOperator(
         task_id='storeGeoPositions',
         http_conn_id="httpPlaces",
         method='GET',
         endpoint="api/storeGeoPositions/"+city,
    )
    
    storeRawPlacesTask = SimpleHttpOperator(
         task_id='ingestGeoPositions',
         http_conn_id="httpPlaces",
         method='GET',
         endpoint="api/ingestGeoPositions/"+city,
    )

    storeInterestingPlacesTask = SimpleHttpOperator(
         task_id='ingestRawPlaces',
         http_conn_id="httpPlaces",
         method='GET',
         endpoint="api/ingestRawPlaces/"+city,
    )
    indexInterestingPlacesTask = SimpleHttpOperator(
         task_id='indexInterestingPlaces',
         http_conn_id="httpPlaces",
         method='GET',
         endpoint="api/indexInterestingPlaces/"+city,
    )
    storePlaceSentimentTask = SimpleHttpOperator(
         task_id='storePlaceSentiment',
         http_conn_id="httpPlaces",
         method='GET',
         endpoint="api/storePlaceSentiment/"+city,
    )
    indexPlaceSentimentTask = SimpleHttpOperator(
         task_id='indexPlaceSentiment',
         http_conn_id="httpPlaces",
         method='GET',
         endpoint="api/indexPlaceSentiment/"+city,
    )

    storeGeoPositionsTask >> storeRawPlacesTask >> storeInterestingPlacesTask >> indexInterestingPlacesTask 
    storeInterestingPlacesTask >> storePlaceSentimentTask >> indexPlaceSentimentTask
