## Database structure

The application stores the location and path information in an internal database. The database management and operation are
obtained from the [ormlite](http://ormlite.com/) library.

The database is composed of the following tables:
<ul>
<li>paths</li>
<li>data</li>
</ul>

The **paths** table holds the timestamps for the beginning of the path.

The **data** table stores the longitude, latitude, altitude and time information.

2015 r-k-
